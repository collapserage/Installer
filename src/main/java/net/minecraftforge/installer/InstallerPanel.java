package net.minecraftforge.installer;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import com.google.common.base.Throwables;

public class InstallerPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private File targetDir;
    private ButtonGroup choiceButtonGroup;
    private JTextField selectedDirText;
    private JLabel infoLabel;
    private JButton sponsorButton;
    private JDialog dialog;
    //private JLabel sponsorLogo;
    private JPanel sponsorPanel;
    private JPanel fileEntryPanel;

    private class FileSelectAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e)
        {
            javax.swing.UIManager.put("FileChooser.acceptAllFileFilterText", "Все файлы");
            javax.swing.UIManager.put("FileChooser.cancelButtonText", "Отмена");
            javax.swing.UIManager.put("FileChooser.cancelButtonToolTipText", "Отмена");
            javax.swing.UIManager.put("FileChooser.deleteFileButtonText", "Удалить");
            javax.swing.UIManager.put("FileChooser.deleteFileButtonToolTipText", "Удалить файл");
            javax.swing.UIManager.put("FileChooser.detailsViewButtonAccessibleName", "Подробно");
            javax.swing.UIManager.put("FileChooser.detailsViewButtonToolTipText", "Подробно");
            javax.swing.UIManager.put("FileChooser.directoryDescriptionText", "Папка");
            javax.swing.UIManager.put("FileChooser.directoryOpenButtonText", "Открыть");
            javax.swing.UIManager.put("FileChooser.directoryOpenButtonToolTipText", "Открыть");
            javax.swing.UIManager.put("FileChooser.enterFilenameLabelText", "Имя");
            javax.swing.UIManager.put("FileChooser.fileDescriptionText", "Описание");
            javax.swing.UIManager.put("FileChooser.fileNameLabelText", "Имя файла");
            javax.swing.UIManager.put("FileChooser.filesLabelText", "Файлы");
            javax.swing.UIManager.put("FileChooser.filesOfTypeLabelText", "Типы файлов");
            javax.swing.UIManager.put("FileChooser.filterLabelText", "Тип(ы) файла");
            javax.swing.UIManager.put("FileChooser.foldersLabelText", "Папка");
            javax.swing.UIManager.put("FileChooser.helpButtonText", "Помощь");
            javax.swing.UIManager.put("FileChooser.helpButtonToolTipText", "Помощь");
            javax.swing.UIManager.put("FileChooser.homeFolderAccessibleName", "Дом");
            javax.swing.UIManager.put("FileChooser.homeFolderToolTipText", "Дом");
            javax.swing.UIManager.put("FileChooser.listViewButtonAccessibleName", "Список");
            javax.swing.UIManager.put("FileChooser.listViewButtonToolTipText", "Список");
            javax.swing.UIManager.put("FileChooser.lookInLabelText", "Католог:");
            javax.swing.UIManager.put("FileChooser.newFolderAccessibleName", "Создать папку");
            javax.swing.UIManager.put("FileChooser.newFolderButtonText", "Создать папку");
            javax.swing.UIManager.put("FileChooser.newFolderButtonToolTipText", "Создать папку");
            javax.swing.UIManager.put("FileChooser.newFolderDialogText", "Создать папку");
            javax.swing.UIManager.put("FileChooser.newFolderErrorSeparator", "Ошибка создания");
            javax.swing.UIManager.put("FileChooser.newFolderErrorText", "Ошибка создания");
            javax.swing.UIManager.put("FileChooser.newFolderToolTipText", "Создать папку");
            javax.swing.UIManager.put("FileChooser.openButtonText", "Открыть");
            javax.swing.UIManager.put("FileChooser.openButtonToolTipText", "Открыть");
            javax.swing.UIManager.put("FileChooser.openDialogTitleText", "Открыть");
            javax.swing.UIManager.put("FileChooser.other.newFolder", "Создать папку");
            javax.swing.UIManager.put("FileChooser.other.newFolder.subsequent", "Создать папку");
            javax.swing.UIManager.put("FileChooser.win32.newFolder", "Создать папку");
            javax.swing.UIManager.put("FileChooser.win32.newFolder.subsequent", "Создать папку");
            javax.swing.UIManager.put("FileChooser.pathLabelText", "Путь");
            javax.swing.UIManager.put("FileChooser.renameFileButtonText", "Переименовать");
            javax.swing.UIManager.put("FileChooser.renameFileButtonToolTipText", "Переименовать");
            javax.swing.UIManager.put("FileChooser.renameFileDialogText", "Переименовать");
            javax.swing.UIManager.put("FileChooser.renameFileErrorText", "Ошибка переименования");
            javax.swing.UIManager.put("FileChooser.renameFileErrorTitle", "Ошибка переименования");
            javax.swing.UIManager.put("FileChooser.saveButtonText", "Сохранить");
            javax.swing.UIManager.put("FileChooser.saveButtonToolTipText", "Сохранить");
            javax.swing.UIManager.put("FileChooser.saveDialogTitleText", "Сохранить");
            javax.swing.UIManager.put("FileChooser.saveInLabelText", "Католог:");
            javax.swing.UIManager.put("FileChooser.updateButtonText", "Обновить");
            javax.swing.UIManager.put("FileChooser.updateButtonToolTipText", "Обновить");
            javax.swing.UIManager.put("FileChooser.upFolderAccessibleName", "Вверх");
            javax.swing.UIManager.put("FileChooser.upFolderToolTipText", "Вверх");

            JFileChooser dirChooser = new JFileChooser();
            dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            dirChooser.setFileHidingEnabled(false);
            dirChooser.ensureFileIsVisible(targetDir);
            dirChooser.setSelectedFile(targetDir);
            int response = dirChooser.showOpenDialog(InstallerPanel.this);
            switch (response)
            {
            case JFileChooser.APPROVE_OPTION:
                targetDir = dirChooser.getSelectedFile();
                updateFilePath();
                break;
            default:
                break;
            }
        }
    }

    private class SelectButtonAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e)
        {
            updateFilePath();
        }

    }
    public InstallerPanel(File targetDir)
    {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        BufferedImage image;
        try
        {
            image = ImageIO.read(SimpleInstaller.class.getResourceAsStream(VersionInfo.getLogoFileName()));
        }
        catch (IOException e)
        {
            throw Throwables.propagate(e);
        }

        JPanel logoSplash = new JPanel();
        logoSplash.setLayout(new BoxLayout(logoSplash, BoxLayout.Y_AXIS));
        ImageIcon icon = new ImageIcon(image);
        JLabel logoLabel = new JLabel(icon);
        logoLabel.setAlignmentX(CENTER_ALIGNMENT);
        logoLabel.setAlignmentY(CENTER_ALIGNMENT);
        logoLabel.setSize(image.getWidth(), image.getHeight());
        logoSplash.add(logoLabel);
        JLabel tag = new JLabel(VersionInfo.getWelcomeMessage());
        tag.setAlignmentX(CENTER_ALIGNMENT);
        tag.setAlignmentY(CENTER_ALIGNMENT);
        logoSplash.add(tag);
        tag = new JLabel(VersionInfo.getVersion());
        tag.setAlignmentX(CENTER_ALIGNMENT);
        tag.setAlignmentY(CENTER_ALIGNMENT);
        logoSplash.add(tag);

        logoSplash.setAlignmentX(CENTER_ALIGNMENT);
        logoSplash.setAlignmentY(TOP_ALIGNMENT);
        this.add(logoSplash);

        sponsorPanel = new JPanel();
        sponsorPanel.setLayout(new BoxLayout(sponsorPanel, BoxLayout.X_AXIS));
        sponsorPanel.setAlignmentX(CENTER_ALIGNMENT);
        sponsorPanel.setAlignmentY(CENTER_ALIGNMENT);

//        sponsorLogo = new JLabel();
//        sponsorLogo.setSize(50, 20);
//        sponsorLogo.setAlignmentX(CENTER_ALIGNMENT);
//        sponsorLogo.setAlignmentY(CENTER_ALIGNMENT);
//        sponsorPanel.add(sponsorLogo);

        sponsorButton = new JButton();
        sponsorButton.setAlignmentX(CENTER_ALIGNMENT);
        sponsorButton.setAlignmentY(CENTER_ALIGNMENT);
        sponsorButton.setBorderPainted(false);
        sponsorButton.setOpaque(false);
        sponsorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Desktop.getDesktop().browse(new URI(sponsorButton.getToolTipText()));
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run()
                        {
                            InstallerPanel.this.dialog.toFront();
                            InstallerPanel.this.dialog.requestFocus();
                        }
                    });
                }
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(InstallerPanel.this, "Произошла ошибка при запуске браузера", "Ошибка при запуске браузера", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        sponsorPanel.add(sponsorButton);

        this.add(sponsorPanel);

        choiceButtonGroup = new ButtonGroup();

        JPanel choicePanel = new JPanel();
        choicePanel.setLayout(new BoxLayout(choicePanel, BoxLayout.Y_AXIS));
        boolean first = true;
        SelectButtonAction sba = new SelectButtonAction();
        for (InstallerAction action : InstallerAction.values())
        {
            if (action == InstallerAction.CLIENT && VersionInfo.hideClient()) continue;
            if (action == InstallerAction.SERVER && VersionInfo.hideServer()) continue;
            if (action == InstallerAction.EXTRACT && VersionInfo.hideExtract()) continue;
            JRadioButton radioButton = new JRadioButton();
            radioButton.setAction(sba);
            radioButton.setText(action.getButtonLabel());
            radioButton.setActionCommand(action.name());
            radioButton.setToolTipText(action.getTooltip());
            radioButton.setSelected(first);
            radioButton.setAlignmentX(LEFT_ALIGNMENT);
            radioButton.setAlignmentY(CENTER_ALIGNMENT);
            choiceButtonGroup.add(radioButton);
            choicePanel.add(radioButton);
            first = false;
        }

        choicePanel.setAlignmentX(RIGHT_ALIGNMENT);
        choicePanel.setAlignmentY(CENTER_ALIGNMENT);
        add(choicePanel);
        JPanel entryPanel = new JPanel();
        entryPanel.setLayout(new BoxLayout(entryPanel,BoxLayout.X_AXIS));

        this.targetDir = targetDir;
        selectedDirText = new JTextField();
        selectedDirText.setEditable(false);
        selectedDirText.setToolTipText("Путь к установленному Minecraft");
        selectedDirText.setColumns(30);
//        homeDir.setMaximumSize(homeDir.getPreferredSize());
        entryPanel.add(selectedDirText);
        JButton dirSelect = new JButton();
        dirSelect.setAction(new FileSelectAction());
        dirSelect.setText("...");
        dirSelect.setToolTipText("Выберите другую папку с установленным Minecraft");
        entryPanel.add(dirSelect);

        entryPanel.setAlignmentX(LEFT_ALIGNMENT);
        entryPanel.setAlignmentY(TOP_ALIGNMENT);
        infoLabel = new JLabel();
        infoLabel.setHorizontalTextPosition(JLabel.LEFT);
        infoLabel.setVerticalTextPosition(JLabel.TOP);
        infoLabel.setAlignmentX(LEFT_ALIGNMENT);
        infoLabel.setAlignmentY(TOP_ALIGNMENT);
        infoLabel.setForeground(Color.RED);
        infoLabel.setVisible(false);

        fileEntryPanel = new JPanel();
        fileEntryPanel.setLayout(new BoxLayout(fileEntryPanel,BoxLayout.Y_AXIS));
        fileEntryPanel.add(infoLabel);
        fileEntryPanel.add(Box.createVerticalGlue());
        fileEntryPanel.add(entryPanel);
        fileEntryPanel.setAlignmentX(CENTER_ALIGNMENT);
        fileEntryPanel.setAlignmentY(TOP_ALIGNMENT);
        this.add(fileEntryPanel);
        updateFilePath();
    }


    private void updateFilePath()
    {
        try
        {
            targetDir = targetDir.getCanonicalFile();
            selectedDirText.setText(targetDir.getPath());
        }
        catch (IOException e)
        {

        }

        InstallerAction action = InstallerAction.valueOf(choiceButtonGroup.getSelection().getActionCommand());
        boolean valid = action.isPathValid(targetDir);

        String sponsorMessage = action.getSponsorMessage();
        if (sponsorMessage != null)
        {
            sponsorButton.setText(sponsorMessage);
            sponsorButton.setToolTipText(action.getSponsorURL());
            if (action.getSponsorLogo() != null)
            {
                sponsorButton.setIcon(action.getSponsorLogo());
            }
            else
            {
                sponsorButton.setIcon(null);
            }
            sponsorPanel.setVisible(true);
        }
        else
        {
            sponsorPanel.setVisible(false);
        }
        if (valid)
        {
            selectedDirText.setForeground(Color.BLACK);
            infoLabel.setVisible(false);
            fileEntryPanel.setBorder(null);
        }
        else
        {
            selectedDirText.setForeground(Color.RED);
            fileEntryPanel.setBorder(new LineBorder(Color.RED));
            infoLabel.setText("<html>"+action.getFileError(targetDir)+"</html>");
            infoLabel.setVisible(true);
        }
        if (dialog!=null)
        {
            dialog.invalidate();
            dialog.pack();
        }
    }

    public void run()
    {
        javax.swing.UIManager.put("OptionPane.okButtonText", "OK");
        javax.swing.UIManager.put("OptionPane.cancelButtonText", "Отмена");
        java.util.Locale.setDefault(new java.util.Locale("ru"));
        JOptionPane optionPane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

        Frame emptyFrame = new Frame("Установщик Forge");
        emptyFrame.setUndecorated(true);
        emptyFrame.setVisible(true);
        emptyFrame.setLocationRelativeTo(null);
        dialog = optionPane.createDialog(emptyFrame, "Установщик Forge");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
        int result = (Integer) (optionPane.getValue() != null ? optionPane.getValue() : -1);
        if (result == JOptionPane.OK_OPTION)
        {
            InstallerAction action = InstallerAction.valueOf(choiceButtonGroup.getSelection().getActionCommand());
            if (action.run(targetDir))
            {
                JOptionPane.showMessageDialog(null, action.getSuccessMessage(), "Готово", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        dialog.dispose();
        emptyFrame.dispose();
    }
}
