package net.minecraftforge.installer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.OutputSupplier;

public class VersionInfo {
    public static final VersionInfo INSTANCE = new VersionInfo();
    public final JsonRootNode versionData;

    public VersionInfo()
    {
        InputStream installProfile = getClass().getResourceAsStream("/install_profile.json");
        JdomParser parser = new JdomParser();

        try
        {
            versionData = parser.parse(new InputStreamReader(installProfile, Charsets.UTF_8));
        }
        catch (Exception e)
        {
            throw Throwables.propagate(e);
        }
    }

    public static String getProfileName()
    {
        return INSTANCE.versionData.getStringValue("install","profileName");
    }

    public static String getVersionTarget()
    {
        return INSTANCE.versionData.getStringValue("install","target");
    }
    public static File getLibraryPath(File root)
    {
        String path = INSTANCE.versionData.getStringValue("install","path");
        String[] split = Iterables.toArray(Splitter.on(':').omitEmptyStrings().split(path), String.class);
        File dest = root;
        Iterable<String> subSplit = Splitter.on('.').omitEmptyStrings().split(split[0]);
        for (String part : subSplit)
        {
            dest = new File(dest, part);
        }
        dest = new File(new File(dest, split[1]), split[2]);
        String fileName = split[1]+"-"+split[2]+".jar";
        return new File(dest,fileName);
    }

    public static String getVersion()
    {
        return INSTANCE.versionData.getStringValue("install","version");
    }

    public static String getWelcomeMessage()
    {
        return "Вас приветствует установщик Forge";
    }

    public static String getLogoFileName()
    {
        return INSTANCE.versionData.getStringValue("install","logo");
    }

    public static boolean getStripMetaInf()
    {
        try
        {
            return INSTANCE.versionData.getBooleanValue("install", "stripMeta");
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static JsonNode getVersionInfo()
    {
        return INSTANCE.versionData.getNode("versionInfo");
    }

    public static File getMinecraftFile(File path)
    {
        return new File(new File(path, getMinecraftVersion()),getMinecraftVersion()+".jar");
    }
    public static String getContainedFile()
    {
        return INSTANCE.versionData.getStringValue("install","filePath");
    }
    public static void extractFile(File path) throws IOException
    {
        INSTANCE.doFileExtract(path);
    }

    private void doFileExtract(File path) throws IOException
    {
        if (Strings.isNullOrEmpty(getContainedFile())) return;
        InputStream inputStream = getClass().getResourceAsStream("/"+getContainedFile());
        OutputSupplier<FileOutputStream> outputSupplier = Files.newOutputStreamSupplier(path);
        ByteStreams.copy(inputStream, outputSupplier);
    }

    public static String getMinecraftVersion()
    {
        return INSTANCE.versionData.getStringValue("install","minecraft");
    }

    public static String getMirrorListURL()
    {
        return INSTANCE.versionData.getStringValue("install","mirrorList");
    }

    public static boolean hasMirrors()
    {
        return INSTANCE.versionData.isStringValue("install","mirrorList");
    }

    public static boolean hideClient()
    {
        return INSTANCE.versionData.isBooleanValue("install", "hideClient") &&
                INSTANCE.versionData.getBooleanValue("install", "hideClient");
    }

    public static boolean hideServer()
    {
        return INSTANCE.versionData.isBooleanValue("install", "hideServer") &&
                INSTANCE.versionData.getBooleanValue("install", "hideServer");
    }

    public static boolean hideExtract()
    {
        return INSTANCE.versionData.isBooleanValue("install", "hideExtract") &&
                INSTANCE.versionData.getBooleanValue("install", "hideExtract");
    }

    public static boolean isInheritedJson()
    {
        return INSTANCE.versionData.isStringValue("versionInfo", "inheritsFrom") &&
                INSTANCE.versionData.isStringValue("versionInfo", "jar");
    }
}
