package submodule;

import com.intellij.openapi.ui.Messages;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class ChageSubmoudleDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField1;
    private JPanel checkboxContainer;
    private JTextArea putContainer;
    private JTextField sourceBranch;
    private JPanel sourceContainer;
    private String rootPath;


    private HashMap<String, SubModuleParse> subModuleParseMap;
    private boolean newBranch;


    public ChageSubmoudleDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public void setNewBranch(boolean newBranch) {
        this.newBranch = newBranch;
        if (newBranch) {
            sourceContainer.setVisible(true);
        } else {
            sourceContainer.setVisible(false);
        }
    }


    public boolean setGitModulePath(String gitModulePath, String rootPath) {
        String content = SubmoduleUtils.readFile(gitModulePath);
        if (StringUtils.isEmpty(content)) {
            Messages.showErrorDialog("请确认.gitmoudles文件是否为空", "错误");
            onCancel();
            return false;
        }
        this.rootPath = rootPath;
        subModuleParseMap = SubmoduleUtils.parseModules(content);
        if (subModuleParseMap == null || subModuleParseMap.size() == 0) {
            Messages.showErrorDialog("请确认.gitmoudles文件格式", "错误");
            onCancel();
            return false;
        }
        checkboxContainer.removeAll();
        checkboxContainer.setLayout(new GridLayout(0, 4));
        for (Map.Entry<String, SubModuleParse> item : subModuleParseMap.entrySet()) {
            JCheckBox checkBox = new JCheckBox();
            checkBox.setText(item.getKey());
            checkboxContainer.add(checkBox);
        }
        this.pack();
        return true;
    }

    private void onOK() {
        // add your code here
        if (StringUtils.isEmpty(textField1.getText())) {
            Messages.showErrorDialog("请输入分支名", "错误");
            return;
        }
        putContainer.setText("");
        putContainer.paintImmediately(putContainer.getBounds());
        try {
            checkBranch(newBranch);
        } catch (SubmoduleUtils.ShellThrow e) {
            printOut(e.throwContent);
            if (e.throwContent.contains("did not match any file(s) known to git")) {
                printOut("确认分支是否存在！");
            }
            return;
        }
        Messages.showInfoMessage("完成", "info");
        buttonOK.setVisible(false);
    }

    /**
     * 切换分支
     */
    private void checkBranch(boolean newBranch) throws SubmoduleUtils.ShellThrow {
        if (StringUtils.isEmpty(textField1.getText())) {
            Messages.showErrorDialog("请输入分支名", "错误");
            return;
        }
        String branchNameStr = textField1.getText();
        if (newBranch) {
            if (StringUtils.isEmpty(sourceBranch.getText())) {
                Messages.showErrorDialog("请输入原始分支", "错误");
                return;
            }
            SubmoduleGitUtils.addMainRemoteGit("android", rootPath, putContainer);
            SubmoduleGitUtils.fetchRemote("android", sourceBranch.getText(), rootPath, putContainer);
            SubmoduleGitUtils.checkoutBranch(branchNameStr, SubmoduleGitUtils.getRemoteName("android") + "/"+ sourceBranch.getText(), rootPath, true, putContainer);
            printOut("主分支创建完成\n");
        } else {
            printOut(SubmoduleUtils.callShell("git checkout " + branchNameStr, rootPath));
            printOut("主分支切换完成\n");
        }
        printOut(SubmoduleUtils.callShell("git submodule sync ", rootPath));
        printOut("submodule sync 完成\n");
        printOut(SubmoduleUtils.callShell("git submodule update ", rootPath, true));
        printOut("submodule update 完成\n");
        checkoutModule(newBranch);
    }

    private void checkoutModule(boolean newBranch) throws SubmoduleUtils.ShellThrow {
        for (int i = 0; i < checkboxContainer.getComponentCount(); i++) {
            Object checkbox = checkboxContainer.getComponent(i);
            if (checkbox instanceof JCheckBox) {
                if (((JCheckBox) checkbox).isSelected()) {
                    String key = ((JCheckBox) checkbox).getText();
                    if (subModuleParseMap.containsKey(key)) {
                        String path = rootPath + "/" + subModuleParseMap.get(key).path;
                        if (newBranch) {
                            SubmoduleGitUtils.checkoutModuleBranch(textField1.getText(), false, path, putContainer);
                        } else {
                            printOut("准备切换" + rootPath + "\n");
                            SubmoduleGitUtils.checkoutBranch(textField1.getText(), "", path, false, putContainer);
                        }
                        printOut("module" + key + "--checkout 完成\n");

                    }
                }
            }
        }
    }

    private void printOut(String put) {
        putContainer.append(put);
        putContainer.paintImmediately(putContainer.getBounds());
    }


    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        ChageSubmoudleDialog dialog = new ChageSubmoudleDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }


}
