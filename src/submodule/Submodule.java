package submodule;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;

public class Submodule extends DefaultActionGroup {

    private String directoryPath = "";

    @Override
    public void update(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        IdeView view = (IdeView) LangDataKeys.IDE_VIEW.getData(dataContext);
        if (view != null) {
            PsiDirectory dir = view.getOrChooseDirectory();
            String directoryPath = dir.getVirtualFile().getPath();
            if (directoryPath != null && directoryPath.equals(e.getProject().getBasePath())){
                e.getPresentation().setEnabled(true);
            }else{
                e.getPresentation().setEnabled(false);
            }
        }
        super.update(e);
    }
}
