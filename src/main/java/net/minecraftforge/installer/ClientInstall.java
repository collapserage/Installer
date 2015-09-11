package net.minecraftforge.installer;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;

import argo.format.PrettyJsonFormatter;
import argo.jdom.JdomParser;
import argo.jdom.JsonField;
import argo.jdom.JsonNode;
import argo.jdom.JsonNodeFactories;
import argo.jdom.JsonRootNode;
import argo.jdom.JsonStringNode;
import argo.saj.InvalidSyntaxException;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

public class ClientInstall implements ActionType {
    //private int selectedMirror;
    private List<Artifact> grabbed;

    @Override
    public boolean run(File target)
    {
        if (!target.exists())
        {
            JOptionPane.showMessageDialog(null, "В этой папке нет файлов Minecraft!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        File launcherProfiles = new File(target,"launcher_profiles.json");
        if (!launcherProfiles.exists())
        {
            JOptionPane.showMessageDialog(null, "В этой папке нет профиля лаунчера, снаначала нужно запустить лаунчер!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        File versionRootDir = new File(target,"versions");
        File versionTarget = new File(versionRootDir,VersionInfo.getVersionTarget());
        if (!versionTarget.mkdirs() && !versionTarget.isDirectory())
        {
            if (!versionTarget.delete())
            {
                JOptionPane.showMessageDialog(null, "Найдена ошибка с данными версии лаунчера. Нужно удалить "+versionTarget.getAbsolutePath()+" вручную", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                versionTarget.mkdirs();
            }
        }

        File librariesDir = new File(target, "libraries");
        IMonitor monitor = DownloadUtils.buildMonitor();
        List<JsonNode> libraries = VersionInfo.getVersionInfo().getArrayNode("libraries");
        monitor.setMaximum(libraries.size() + 3);
        int progress = 3;

        File versionJsonFile = new File(versionTarget,VersionInfo.getVersionTarget()+".json");

        if (!VersionInfo.isInheritedJson())
        {
            File clientJarFile = new File(versionTarget, VersionInfo.getVersionTarget()+".jar");
            File minecraftJarFile = VersionInfo.getMinecraftFile(versionRootDir);

            try
            {
                boolean delete = false;
                monitor.setNote("Проверка jar-файла клиента Minecraft");
                monitor.setProgress(1);

                if (!minecraftJarFile.exists())
                {
                    minecraftJarFile = File.createTempFile("minecraft_client", ".jar");
                    delete = true;
                    monitor.setNote(String.format("Скачивание клиента Minecraft %s", VersionInfo.getMinecraftVersion()));
                    String clientUrl = String.format(DownloadUtils.VERSION_URL_CLIENT.replace("{MCVER}", VersionInfo.getMinecraftVersion()));
                    System.out.println("  Temp File: " + minecraftJarFile.getAbsolutePath());

                    if (!DownloadUtils.downloadFileEtag("minecraft server", minecraftJarFile, clientUrl))
                    {
                        minecraftJarFile.delete();
                        JOptionPane.showMessageDialog(null, "Скачивание клиента неудачно, неправильная контрольная сумма e-tag.\n" +
                                "Попробуйте ещё раз или используйте официальный лаунчер для запуска Minecraft " +
                                VersionInfo.getMinecraftVersion() + ".",
                                "Ошибка при загрузке", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    monitor.setProgress(2);
                }

                if (VersionInfo.getStripMetaInf())
                {
                    monitor.setNote("Копирование и обработка jar-файла клиента");
                    copyAndStrip(minecraftJarFile, clientJarFile);
                    monitor.setProgress(3);
                }
                else
                {
                    monitor.setNote("Копирование jar-файла клиента");
                    Files.copy(minecraftJarFile, clientJarFile);
                    monitor.setProgress(3);
                }

                if (delete)
                {
                    minecraftJarFile.delete();
                }
            }
            catch (IOException e1)
            {
                JOptionPane.showMessageDialog(null, "Нужно запустить версию "+VersionInfo.getMinecraftVersion()+" вручную хотя бы раз", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        File targetLibraryFile = VersionInfo.getLibraryPath(librariesDir);
        grabbed = Lists.newArrayList();
        List<Artifact> bad = Lists.newArrayList();
        progress = DownloadUtils.downloadInstalledLibraries("clientreq", librariesDir, monitor, libraries, progress, grabbed, bad);

        monitor.close();
        if (bad.size() > 0)
        {
            String list = Joiner.on("\n").join(bad);
            JOptionPane.showMessageDialog(null, "Эти библиотеки не удалось скачать. Попробуйте ещё раз.\n"+list, "Ошибка при загрузке", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!targetLibraryFile.getParentFile().mkdirs() && !targetLibraryFile.getParentFile().isDirectory())
        {
            if (!targetLibraryFile.getParentFile().delete())
            {
                JOptionPane.showMessageDialog(null, "Найдена ошибка с данными версии лаунчера. Нужно удалить "+targetLibraryFile.getAbsolutePath()+" вручную", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            else
            {
                targetLibraryFile.getParentFile().mkdirs();
            }
        }


        JsonRootNode versionJson = JsonNodeFactories.object(VersionInfo.getVersionInfo().getFields());

        try
        {
            BufferedWriter newWriter = Files.newWriter(versionJsonFile, Charsets.UTF_8);
            PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter().format(versionJson,newWriter);
            newWriter.close();
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(null, "Произошла проблема при сохранении данных лаунчера, папка защищена от записи?", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try
        {
            VersionInfo.extractFile(targetLibraryFile);
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(null, "Произошла проблема при записи файла системной библиотеки", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        JdomParser parser = new JdomParser();
        JsonRootNode jsonProfileData;

        try
        {
            jsonProfileData = parser.parse(Files.newReader(launcherProfiles, Charsets.UTF_8));
        }
        catch (InvalidSyntaxException e)
        {
            JOptionPane.showMessageDialog(null, "Файл профиля лаунчера повреждён. Запустите лаунчер, чтобы исправить это!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        catch (Exception e)
        {
            throw Throwables.propagate(e);
        }


        

        HashMap<JsonStringNode, JsonNode> profileCopy = Maps.newHashMap(jsonProfileData.getNode("profiles").getFields());
        HashMap<JsonStringNode, JsonNode> rootCopy = Maps.newHashMap(jsonProfileData.getFields());
        if(profileCopy.containsKey(JsonNodeFactories.string(VersionInfo.getProfileName())))
        {
            HashMap<JsonStringNode, JsonNode> forgeProfileCopy = Maps.newHashMap(profileCopy.get(JsonNodeFactories.string(VersionInfo.getProfileName())).getFields());
            forgeProfileCopy.put(JsonNodeFactories.string("name"), JsonNodeFactories.string(VersionInfo.getProfileName()));
            forgeProfileCopy.put(JsonNodeFactories.string("lastVersionId"), JsonNodeFactories.string(VersionInfo.getVersionTarget()));
        }
        else
        {
            JsonField[] fields = new JsonField[] {
                JsonNodeFactories.field("name", JsonNodeFactories.string(VersionInfo.getProfileName())),
                JsonNodeFactories.field("lastVersionId", JsonNodeFactories.string(VersionInfo.getVersionTarget())),
            };
            profileCopy.put(JsonNodeFactories.string(VersionInfo.getProfileName()), JsonNodeFactories.object(fields));
        }
        JsonRootNode profileJsonCopy = JsonNodeFactories.object(profileCopy);
        rootCopy.put(JsonNodeFactories.string("profiles"), profileJsonCopy);

        jsonProfileData = JsonNodeFactories.object(rootCopy);

        try
        {
            BufferedWriter newWriter = Files.newWriter(launcherProfiles, Charsets.UTF_8);
            PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter().format(jsonProfileData,newWriter);
            newWriter.close();
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(null, "Произошла проблема при сохранении профиля для запуска, папка защищена от записи?", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private void copyAndStrip(File sourceJar, File targetJar) throws IOException
    {
        ZipFile in = new ZipFile(sourceJar);
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(targetJar)));

        for (ZipEntry e : Collections.list(in.entries()))
        {
            if (e.isDirectory())
            {
                out.putNextEntry(e);
            }
            else if (e.getName().startsWith("META-INF"))
            {
            }
            else
            {
                ZipEntry n = new ZipEntry(e.getName());
                n.setTime(e.getTime());
                out.putNextEntry(n);
                out.write(readEntry(in, e));
            }
        }

        in.close();
        out.close();
    }

    private static byte[] readEntry(ZipFile inFile, ZipEntry entry) throws IOException
    {
        return readFully(inFile.getInputStream(entry));
    }

    private static byte[] readFully(InputStream stream) throws IOException
    {
        byte[] data = new byte[4096];
        ByteArrayOutputStream entryBuffer = new ByteArrayOutputStream();
        int len;
        do
        {
            len = stream.read(data);
            if (len > 0)
            {
                entryBuffer.write(data, 0, len);
            }
        } while (len != -1);

        return entryBuffer.toByteArray();
    }

    @Override
    public boolean isPathValid(File targetDir)
    {
        if (targetDir.exists())
        {
            File launcherProfiles = new File(targetDir,"launcher_profiles.json");
            return launcherProfiles.exists();
        }
        return false;
    }


    @Override
    public String getFileError(File targetDir)
    {
        if (targetDir.exists())
        {
            return "В папке отсутствует профиль лаунчера. Сначала запустите лаунчер Minecraft";
        }
        else
        {
            return "Отсутствует папка Minecraft. Выберите другую либо запустите лаунчер, чтобы создать её";
        }
    }

    @Override
    public String getSuccessMessage()
    {
        if (grabbed.size() > 0)
        {
            return String.format("Профиль %s для версии %s успешно установлен (загружено необходимых библиотек - %s)", VersionInfo.getProfileName(), VersionInfo.getVersion(), grabbed.size());
        }
        return String.format("Профиль %s для версии %s успешно установлен в лаунчер", VersionInfo.getProfileName(), VersionInfo.getVersion());
    }

    @Override
    public String getSponsorMessage()
    {
        return MirrorData.INSTANCE.hasMirrors() ? String.format("<html><a href=\'%s\'>Данные любезно предоставлены %s</a></html>", MirrorData.INSTANCE.getSponsorURL(),MirrorData.INSTANCE.getSponsorName()) : null;
    }
}
