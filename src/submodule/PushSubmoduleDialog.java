package submodule;

import com.intellij.openapi.ui.Messages;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PushSubmoduleDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox gitPath;
    private JTextArea putContainer;
    private JTextArea commit;
    private JPanel checkboxContainer;


    private String rootPath;
    private LinkedHashMap<String, SubModuleParse> subModuleParseMap;
    private String gitModulePath;

    public PushSubmoduleDialog() {
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
        this.gitModulePath = gitModulePath;
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

        try {
            for (Map.Entry<String, SubModuleParse> item : subModuleParseMap.entrySet()) {
                String path = rootPath + "/" + item.getValue().path;
                String banchStatus = SubmoduleUtils.callShell("git status", path);
                if (banchStatus.contains("On branch ")) {
                    String pattern = "On branch (\\S*)";
                    Pattern r = Pattern.compile(pattern);
                    // 现在创建 matcher 对象
                    Matcher m = r.matcher(banchStatus);
                    String branch = "";
                    if (m.find()) {
                        branch = m.group(1).trim();
                        printOut("当前" + item.getValue().module + "分支处于" + branch + "可push\n");
                        if (banchStatus.contains("Changes not staged for commit")) {
                            Messages.showErrorDialog(item.getValue().module + "还有没有add的文件 请处理后运行！", "error");
                            onCancel();
                            return false;
                        } else if (banchStatus.contains("Changes to be committed")) {
                            printOut("当前" + item.getValue().module + "分支" + branch + "还有代码需要commit\n");
                        }
                        JCheckBox checkBox = new JCheckBox();
                        checkBox.setText(item.getKey());
                        checkboxContainer.add(checkBox);
                    }
                }
            }
            ArrayList<String> remoteList = SubmoduleGitUtils.getTantanRemotes(rootPath);
            for (String item : remoteList) {
                gitPath.addItem(item);
            }
            gitPath.setSelectedIndex(-1);
        } catch (SubmoduleUtils.ShellThrow e) {
            printOut(e.throwContent);
        }
        this.pack();
        return true;
    }

    private void printOut(String put) {
        putContainer.append(put);
        putContainer.paintImmediately(putContainer.getBounds());
    }

    private void onOK() {
        // add your code here
        String gitPathStr = this.gitPath.getEditor().getItem().toString().trim();
        String commitStr = SubmoduleUtils.replaceBlank(commit.getText());
        if (StringUtils.isEmpty(gitPathStr)) {
            Messages.showErrorDialog("请输入远端地址", "error");
            return;
        }
        putContainer.setText("");
        putContainer.paintImmediately(putContainer.getBounds());
        try {
            LinkedHashMap<String, SubModuleParse> map = (LinkedHashMap<String, SubModuleParse>) subModuleParseMap.clone();
            String banchStatus = SubmoduleUtils.callShell("git status", rootPath);
            if (banchStatus.contains("Changes to be committed")) {
                if (StringUtils.isEmpty(commitStr)) {
                    throw new SubmoduleUtils.ShellThrow("putong——module没有commit 请添加commit信息");
                }
                printOut(SubmoduleUtils.callShell("git commit -m \"" + commitStr + "\"", rootPath));
                printOut("putong:module_commit完成");
            }

            //更改.gitmodule 文件
            if (dealModule(rootPath, commitStr, gitPathStr, map)) {
                SubmoduleUtils.writeModules(map, gitModulePath);
            }
            //更改module commitId
            int count = checkboxContainer.getComponentCount();
            for (int i = 0; i < count; i++) {
                JCheckBox checkBox = (JCheckBox) checkboxContainer.getComponent(i);
                String module = checkBox.getText();
                if (map.containsKey(module)) {
                    printOut(SubmoduleUtils.callShell("git add " + map.get(module).path, rootPath));
                }
            }
            //更改.gitmodule
            printOut(SubmoduleUtils.callShell("git add " + gitModulePath, rootPath));
            //提交更改
            if (StringUtils.isEmpty(commitStr)) {
                printOut(SubmoduleUtils.callShell("git commit -m \"gitmodule等修改\" ", rootPath));
            } else {
                printOut(SubmoduleUtils.callShell("git commit -m \"gitmodule等修改&" + commitStr + "\" ", rootPath));
            }
            SubmoduleGitUtils.addMainRemoteGit(gitPathStr, rootPath, putContainer);
            printOut("putong:module_commit完成");
            printOut("开始push 可能有超时风险 10s");
            printOut(SubmoduleUtils.callShell("git push " + SubmoduleGitUtils.getRemoteName(gitPathStr), rootPath, true));
        } catch (SubmoduleUtils.ShellThrow shellThrow) {
            printOut(shellThrow.throwContent);
            return;
        }
        Messages.showInfoMessage("完成", "info");
        buttonOK.setVisible(false);


    }

    private boolean dealModule(String rootPath, String commitStr, String gitPathStr, HashMap<String, SubModuleParse> map) throws SubmoduleUtils.ShellThrow {
        int count = checkboxContainer.getComponentCount();
        boolean haveChange = false;
        for (int i = 0; i < count; i++) {
            JCheckBox checkBox = (JCheckBox) checkboxContainer.getComponent(i);
            String module = checkBox.getText();
            if (map.containsKey(module)) {
                String path = rootPath + "/" + map.get(module).path;
                String banchStatus = SubmoduleUtils.callShell("git status", path);


                if (banchStatus.contains("Changes to be committed")) {
                    if (StringUtils.isEmpty(commitStr)) {
                        throw new SubmoduleUtils.ShellThrow(module + "module没有commit 请添加commit信息");
                    }
                    printOut(SubmoduleUtils.callShell("git commit -m \"" + commitStr + "\"", path));
                    printOut(module + ":module——commit完成");
                }

                if (!map.get(module).url.contains(gitPathStr)) {
                    String oldGitPathStr = matchModuleUrl(map.get(module).url);
                    if (!StringUtils.isEmpty(oldGitPathStr)) {
                        map.get(module).url = map.get(module).url.replace(oldGitPathStr, gitPathStr);
                    }
                    haveChange = true;
                }
                SubmoduleGitUtils.addRemoteGit(gitPathStr, path, map.get(module).url, putContainer);
                printOut("开始push 可能有超时风险 10s");
                printOut(SubmoduleUtils.callShell("git push " + SubmoduleGitUtils.getRemoteName(gitPathStr), path, true));

                String currentBranch = matchStatusBranch(banchStatus);
                if (!StringUtils.isEmpty(currentBranch) && !map.get(module).branch.equals(currentBranch)) {
                    map.get(module).branch = currentBranch;
                    haveChange = true;
                }
            }
        }
        return haveChange;
    }

    private String matchModuleUrl(String url) {
        String pattern = "git@gitlab.p1staff.com:(\\S*)/[\\w_-]*.git";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(url);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    private String matchStatusBranch(String branch) {
        String pattern = "On branch (\\S*)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(branch);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        PushSubmoduleDialog dialog = new PushSubmoduleDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
