package submodule;

import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubmoduleGitUtils {


    public static void addMainRemoteGit(String userPath, String rootPath, JTextArea textArea) throws SubmoduleUtils.ShellThrow {
        addRemoteGit(userPath, rootPath, "git@gitlab.p1staff.com:" + userPath + "/putong-android.git", textArea);
    }

    public static void addRemoteGit(String userPath, String rootPath, String gitPath, JTextArea textArea) throws SubmoduleUtils.ShellThrow {
        String remoteName = getRemoteName(userPath);
        printOut("开始尝试获取本地远端仓库目录\n", textArea);
        String remoteStr = SubmoduleUtils.callShell("git remote -v", rootPath);
        if (!remoteStr.contains(remoteName)) {
            printOut(SubmoduleUtils.callShell("git remote add " + remoteName + " " + gitPath, rootPath), textArea);
            printOut(remoteName + "远程仓库添加完成\n", textArea);
        }
    }


    public static void fetchRemote(String remoteNick, String branchName, String rootPath, JTextArea textArea) throws SubmoduleUtils.ShellThrow {
        String remoteName = getRemoteName(remoteNick);
        printOut("开始拉取远程分支" + branchName + " 可能有超时风险10s\n", textArea);
        printOut(SubmoduleUtils.callShell("git fetch " + remoteName + " " + branchName, rootPath, true), textArea);
        printOut("远程分支" + branchName + "拉取完成\n", textArea);
    }

    public static void checkoutBranch(String newBranchName, String targetBranchName, String rootPath, boolean createBranch, JTextArea textArea) throws SubmoduleUtils.ShellThrow {
        if (createBranch) {
            try {
                if (!StringUtils.isEmpty(targetBranchName)) {
                    printOut(SubmoduleUtils.callShell("git checkout -b " + newBranchName + " " + targetBranchName, rootPath), textArea);
                } else {
                    printOut(SubmoduleUtils.callShell("git checkout -b " + newBranchName, rootPath), textArea);
                }
            } catch (SubmoduleUtils.ShellThrow shellThrow) {
                if (!shellThrow.throwContent.contains("already exists")) {
                    throw shellThrow;
                }
                printOut(newBranchName + "已经存在 现在切换到本地\n", textArea);
            }
        }
        printOut(SubmoduleUtils.callShell("git checkout " + newBranchName, rootPath), textArea);
    }

    public static void checkoutModuleBranch(String newBranch, boolean update, String rootPath, JTextArea textArea) throws SubmoduleUtils.ShellThrow {
        printOut("准备切换" + rootPath + "\n", textArea);
        String banchStatus = SubmoduleUtils.callShell("git status", rootPath);
        String head = null;
        if (banchStatus.contains("HEAD detached at")) {
            String pattern = "HEAD detached at (\\w*)";
            Pattern r = Pattern.compile(pattern);
            // 现在创建 matcher 对象
            Matcher m = r.matcher(banchStatus);
            if (m.find()) {
                head = m.group(1).trim();
            }
            printOut("当前游离分支:" + head + "\n", textArea);
            checkoutBranch(newBranch, "", rootPath, true, textArea);
            if (update && !StringUtils.isEmpty(head)) {
                printOut(SubmoduleUtils.callShell("git merge " + head, rootPath), textArea);
                printOut("merge " + head + "完成\n", textArea);
            }
        } else if (banchStatus.contains("On branch " + newBranch)) {
            printOut("当前已经处在" + newBranch + "分支\n", textArea);
            checkoutBranch(newBranch, "", rootPath, false, textArea);
        } else {
            throw new SubmoduleUtils.ShellThrow(rootPath + "当前处于其他分支请自行解决后在运行！");
        }
    }


    public static String getRemoteName(String remoteNick) {
        return "tantan_submodule_" + remoteNick;
    }

    private static void printOut(String str, JTextArea textArea) {
        if (textArea != null) {
            textArea.append(str);
            textArea.paintImmediately(textArea.getBounds());
        }
    }

    public static ArrayList<String> getTantanRemotes(String rootPath) throws SubmoduleUtils.ShellThrow {
        ArrayList<String> tantanRemoteList = new ArrayList<>();
        String result = SubmoduleUtils.callShell("git remote ", rootPath);
        String[] remoteAllList = result.split("\n");
        if (remoteAllList != null && remoteAllList.length > 1) {
            for (String item : remoteAllList) {
                if (item.startsWith("tantan_submodule_")) {
                    tantanRemoteList.add(item.trim().replace("tantan_submodule_", ""));
                }
            }
        }
        return tantanRemoteList;
    }

    public static ArrayList<String> getTantanBranchs(String rootPath) throws SubmoduleUtils.ShellThrow {
        ArrayList<String> tantanRemoteList = new ArrayList<>();
        String result = SubmoduleUtils.callShell("git branch", rootPath);
        String[] remoteAllList = result.split("\n");
        if (remoteAllList != null && remoteAllList.length > 1) {
            for (String item : remoteAllList) {
                if (!item.startsWith("执行")) {
                    String banchName=item.substring(2,item.length());
                    tantanRemoteList.add(banchName);
                }
            }
        }
        return tantanRemoteList;
    }


}


