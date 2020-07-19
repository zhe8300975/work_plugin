package submodule;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;


public class RunHolderScript extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        VirtualFile[] files = (VirtualFile[]) e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (files != null && files.length > 0) {
            if (files[0].getFileType() instanceof XmlFileType) {
                String[] paths = files[0].getPath().split("/");
                if (paths.length > 1 && "layout".equals(paths[paths.length - 2])) {
                    e.getPresentation().setEnabled(true);
                    return;
                }
            }
        }
        e.getPresentation().setEnabled(false);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        VirtualFile[] files = (VirtualFile[]) e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        String content = SubmoduleUtils.readFile(e.getProject().getBasePath() + "/.gitmodules");
        if (StringUtils.isEmpty(content)) {
            Notification notification = new Notification("Tantan", "不是探探项目", "没有gitmodules文件", NotificationType.ERROR);
            Notifications.Bus.notify(notification);
            return;
        }
        LinkedHashMap<String, SubModuleParse> subModuleParseMap = SubmoduleUtils.parseModules(content);
        if (subModuleParseMap == null || subModuleParseMap.size() == 0) {
            Notification notification = new Notification("Tantan", "不是探探项目", "没有gitmodules文件格式不符合", NotificationType.ERROR);
            Notifications.Bus.notify(notification);
            return;
        }
        String filePath = files[0].getPath();
        for (Map.Entry<String, SubModuleParse> item : subModuleParseMap.entrySet()) {
            String rootPath = e.getProject().getBasePath() + "/" + item.getValue().path;
            if (filePath.startsWith(rootPath)) {
                try {
                    Notification notification = new Notification("Tantan", "holders脚本开始执行", rootPath, NotificationType.INFORMATION);
                    Notifications.Bus.notify(notification);
                    String result = SubmoduleUtils.callShell("./holders.bat " + filePath, rootPath, false, true);
                    notification = new Notification("Tantan", "holders脚本执行成功", result, NotificationType.INFORMATION);
                    Notifications.Bus.notify(notification);
                } catch (SubmoduleUtils.ShellThrow shellThrow) {
                    Notification notification = new Notification("Tantan", "holders脚本执行失败", shellThrow.throwContent, NotificationType.ERROR);
                    Notifications.Bus.notify(notification);
                }
            }

        }
    }

}
