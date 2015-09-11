package net.minecraftforge.installer;

import java.io.File;
import javax.swing.Icon;

import com.google.common.base.Throwables;

public enum InstallerAction {
    CLIENT("Установить клиент", "Установить новый профиль в лаунчер клиента", ClientInstall.class),
    SERVER("Установить сервер", "Создать новый сервер с Forge", ServerInstall.class),
    EXTRACT("Распаковать", "Распаковать содержащийся jar-файл", ExtractAction.class);

    private String label;
    private String tooltip;
    private ActionType action;

    private InstallerAction(String label, String tooltip, Class<? extends ActionType> action)
    {
        this.label = label;
        this.tooltip = tooltip;
        try
        {
            this.action = action.newInstance();
        }
        catch (Exception e)
        {
            throw Throwables.propagate(e);
        }
    }
    public String getButtonLabel()
    {
        return label;
    }

    public String getTooltip()
    {
        return tooltip;
    }

    public boolean run(File path)
    {
        return action.run(path);
    }
    public boolean isPathValid(File targetDir)
    {
        return action.isPathValid(targetDir);
    }

    public String getFileError(File targetDir)
    {
        return action.getFileError(targetDir);
    }
    public String getSuccessMessage()
    {
        return action.getSuccessMessage();
    }
    public String getSponsorMessage()
    {
        return action.getSponsorMessage();
    }

    public Icon getSponsorLogo()
    {
        return MirrorData.INSTANCE.getImageIcon();
    }
    public String getSponsorURL()
    {
        return MirrorData.INSTANCE.getSponsorURL();
    }
}
