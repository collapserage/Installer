package net.minecraftforge.installer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import argo.jdom.JsonNode;

public class ServerInstall implements ActionType {

    public static boolean headless;
    private List<Artifact> grabbed;

    @Override
    public boolean run(File target)
    {
        if (target.exists() && !target.isDirectory())
        {
            if (!headless)
                JOptionPane.showMessageDialog(null, "Эта папка не пуста, сервер не может быть установлен здесь!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        File librariesDir = new File(target,"libraries");
        if (!target.exists())
        {
            target.mkdirs();
        }
        librariesDir.mkdir();
        IMonitor monitor = DownloadUtils.buildMonitor();
        if (headless && MirrorData.INSTANCE.hasMirrors())
        {
            monitor.setNote(getSponsorMessage());
        }
        List<JsonNode> libraries = VersionInfo.getVersionInfo().getArrayNode("libraries");
        monitor.setMaximum(libraries.size() + 2);
        int progress = 2;
        grabbed = Lists.newArrayList();
        List<Artifact> bad = Lists.newArrayList();

        //Download MC Server jar
        String mcServerURL = String.format(DownloadUtils.VERSION_URL_SERVER.replace("{MCVER}", VersionInfo.getMinecraftVersion()));
        File mcServerFile = new File(target,"minecraft_server."+VersionInfo.getMinecraftVersion()+".jar");
        if (!mcServerFile.exists())
        {
            monitor.setNote("Проверка jar-файла сервера");
            monitor.setProgress(1);
            monitor.setNote(String.format("Скачивание сервера Minecraft %s",VersionInfo.getMinecraftVersion()));
            if (!DownloadUtils.downloadFileEtag("minecraft server", mcServerFile, mcServerURL))
            {
                mcServerFile.delete();
                if (!headless)
                {
                    JOptionPane.showMessageDialog(null, "Скачивание сервера неудачно, неправильная контрольная сумма e-tag.\n"+
                                                        "Попробуйте ещё раз или разместите jar-файл сервера вручную",
                                                        "Ошибка при скачивании", JOptionPane.ERROR_MESSAGE);
                }
                else
                {
                    System.err.println("Downloading minecraft server failed, invalid e-tag checksum.");
                    System.err.println("Try again, or manually place server jar to skip download.");
                }
                return false;
            }
            monitor.setProgress(2);
        }
        progress = DownloadUtils.downloadInstalledLibraries("serverreq", librariesDir, monitor, libraries, progress, grabbed, bad);

        monitor.close();
        if (bad.size() > 0)
        {
            String list = Joiner.on("\n").join(bad);
            if (!headless)
                JOptionPane.showMessageDialog(null, "Эти библиотеки не удалось скачать. Попробуйте ещё раз\n"+list, "Ошибка при скачивании", JOptionPane.ERROR_MESSAGE);
            else
                System.err.println("These libraries failed to download, try again. \n"+list);
            return false;
        }
        try
        {
            File targetRun = new File(target,VersionInfo.getContainedFile());
            VersionInfo.extractFile(targetRun);
        }
        catch (IOException e)
        {
            if (!headless)
                JOptionPane.showMessageDialog(null, "Произошла ошибка при установке библиотеки", "Ошибка", JOptionPane.ERROR_MESSAGE);
            else
                System.err.println("An error occurred installing the distributable");
            return false;
        }

        return true;
    }

    @Override
    public boolean isPathValid(File targetDir)
    {
        return targetDir.exists() && targetDir.isDirectory() && targetDir.list().length == 0;
    }

    @Override
    public String getFileError(File targetDir)
    {
        if (!targetDir.exists())
        {
            return "Указанная папка не существует<br/>Она будет создана";
        }
        else if (!targetDir.isDirectory())
        {
            return "Указанный путь должен являться папкой";
        }
        else
        {
            return "В указанной папке уже есть файлы";
        }
    }

    @Override
    public String getSuccessMessage()
    {
        if (grabbed.size() > 0)
        {
            return String.format("Сервер Minecraft успешно скачан (скачано библиотек - %d) и установлен %s", grabbed.size(), VersionInfo.getProfileName());
        }
        return String.format("Сервер Minecraft успешно скачан и установлен %s", VersionInfo.getProfileName());
    }

    @Override
    public String getSponsorMessage()
    {
        return MirrorData.INSTANCE.hasMirrors() ? String.format(headless ? "Данные любезно предоставлены %2$s в %1$s" : "<html><a href=\'%s\'>Данные любезно предоставлены %s</a></html>", MirrorData.INSTANCE.getSponsorURL(),MirrorData.INSTANCE.getSponsorName()) : null;
    }
}
