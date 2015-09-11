package net.minecraftforge.installer;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

public class ExtractAction implements ActionType {

    public static boolean headless;
    @Override
    public boolean run(File target)
    {
        File file = new File(target,VersionInfo.getContainedFile());
        try
        {
            VersionInfo.extractFile(file);
        }
        catch (IOException e)
        {
            if (!headless)
                JOptionPane.showMessageDialog(null, "Произошла ошибка при распаковке файла", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    public boolean isPathValid(File targetDir)
    {
        return targetDir.exists() && targetDir.isDirectory();
    }

    @Override
    public String getFileError(File targetDir)
    {
        return !targetDir.exists() ? "Указанная папка не существует" : !targetDir.isDirectory() ? "Указанный путь не является папкой" : "";
    }

    @Override
    public String getSuccessMessage()
    {
        return "Распаковка успешна";
    }

    @Override
    public String getSponsorMessage()
    {
        return null;
    }
}
