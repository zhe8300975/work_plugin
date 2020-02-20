package submodule;

import com.intellij.openapi.ui.Messages;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.event.*;

public class RemoteBranchDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField gitPath;
    private JTextArea putContainer;
    private JTextField branchName;

    private String rootPath;


    public RemoteBranchDialog() {
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

    public boolean setGitModulePath(String gitModulePath, String rootPath) {
        String content = SubmoduleUtils.readFile(gitModulePath);
        if (StringUtils.isEmpty(content)) {
            Messages.showErrorDialog("请确认.gitmoudles文件是否为空", "错误");
            onCancel();
            return false;
        }
        this.rootPath = rootPath;
        this.pack();
        return true;
    }

    private void onOK() {
        // add your code here
        String gitPathStr = this.gitPath.getText().trim();
        String branchNameStr = branchName.getText();
        if (StringUtils.isEmpty(gitPathStr) || StringUtils.isEmpty(branchNameStr)) {
            Messages.showErrorDialog("请输入git地址/branch分支名", "错误");
        }
        putContainer.setText("");
        putContainer.paintImmediately(putContainer.getBounds());
        try {
            SubmoduleGitUtils.addMainRemoteGit(gitPathStr, rootPath, putContainer);
            SubmoduleGitUtils.fetchRemote(gitPathStr, branchNameStr, rootPath, putContainer);
            SubmoduleGitUtils.checkoutBranch(gitPathStr + "/" + branchNameStr, SubmoduleGitUtils.getRemoteName(gitPathStr) + "/" + branchNameStr, rootPath, true, putContainer);
            printOut(SubmoduleUtils.callShell("git submodule sync ", rootPath));
            printOut("submodule sync 完成\n");
            printOut(SubmoduleUtils.callShell("git submodule update ", rootPath, true));
            printOut("submodule update 完成\n");
        } catch (SubmoduleUtils.ShellThrow e) {
            printOut(e.throwContent);
            return;
        }
        Messages.showInfoMessage("完成", "info");
        buttonOK.setVisible(false);
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
        RemoteBranchDialog dialog = new RemoteBranchDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
