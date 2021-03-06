package cn.alanhe;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.annotate.AnnotationProvider;
import com.intellij.openapi.vcs.annotate.LineAnnotationAspect;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Optional;

public class GitOperationUtil {

    static String getAnnotateAuthor(Project project, VirtualFile file, int currentLineNumber, boolean outsiderFile) throws VcsException {
        if (outsiderFile) {
            VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(getFilePath(file.getUserDataString()));
            return getAnnotateAuthorByProvider(project, fileByPath, currentLineNumber);
        }
        return getAnnotateAuthorByProvider(project, file, currentLineNumber);
    }

    private static String getAnnotateAuthorByProvider(Project project, VirtualFile virtualFile, int currentLineNumber) throws VcsException {
        AbstractVcs abstractVcs = VcsUtil.getVcsFor(project, virtualFile);
        if (abstractVcs == null) {
            return StringUtils.EMPTY;
        }
        AnnotationProvider annotationProvider = abstractVcs.getAnnotationProvider();
        if (annotationProvider == null) {
            return StringUtils.EMPTY;
        }
        LineAnnotationAspect aspect = annotationProvider.annotate(virtualFile).getAspects()[2];
        return aspect.getValue(currentLineNumber);
    }

    public static String getFilePath(String userData) {
        userData = userData.substring(1, userData.length() - 1);
        Optional<String> first = Arrays.stream(userData.split(",")).filter((String item) -> {
            String[] strings = item.split("->");
            return strings[0].trim().equals("OutsidersPsiFileSupport.FilePath");
        }).findFirst();
        return first.map(s -> s.split("->")[1].trim()).orElse(StringUtils.EMPTY);
    }
}
