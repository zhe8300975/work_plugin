package submodule;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;

public class SubmoduleCheckoutBranch extends AnAction {

    private String directoryPath = "";

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        DataContext dataContext = e.getDataContext();
        IdeView view = (IdeView) LangDataKeys.IDE_VIEW.getData(dataContext);
        if (view != null) {
            e.getProject().getBasePath();
            PsiDirectory dir = view.getOrChooseDirectory();
            directoryPath = dir.getVirtualFile().getPath();
            String modulePath = e.getProject().getBasePath() + "/.gitmodules";
            System.out.println(modulePath);
            File file = new File(modulePath);
            if (!file.exists()) {
                Messages.showErrorDialog("请确认.gitmoudles文件是否存在!", "错误");
            } else {
                ChageSubmoudleDialog myDialog = new ChageSubmoudleDialog();
                myDialog.setNewBranch(false);
                myDialog.setMinimumSize(new Dimension(700, 300));
                myDialog.setLocationRelativeTo(null);
                if (myDialog.setGitModulePath(modulePath, e.getProject().getBasePath())) {
                    myDialog.setVisible(true);
                }
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        IdeView view = (IdeView) LangDataKeys.IDE_VIEW.getData(dataContext);
        if (view != null) {
            PsiDirectory dir = view.getOrChooseDirectory();
            String directoryPath = dir.getVirtualFile().getPath();
            if (directoryPath != null && directoryPath.equals(e.getProject().getBasePath())&&directoryPath.endsWith("putong-android")){
                e.getPresentation().setEnabled(true);
            }else{
                e.getPresentation().setEnabled(false);
            }
        }
        super.update(e);
    }
}
