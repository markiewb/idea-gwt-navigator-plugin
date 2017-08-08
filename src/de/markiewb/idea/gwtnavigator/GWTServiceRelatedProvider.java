package de.markiewb.idea.gwtnavigator;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.navigation.GotoRelatedProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by markiewb on 6/14/2017.
 */
public class GWTServiceRelatedProvider extends GotoRelatedProvider {

    @Override
    public List<? extends GotoRelatedItem> getItems(@NotNull PsiElement psiElement) {


        Project project = psiElement.getProject();
//        Settings settings = ServiceManager.getService(project, Settings.class);
        try {

            PsiJavaFileImpl original = (PsiJavaFileImpl) psiElement.getContainingFile();
            String packageName = original.getPackageName();
            String name = original.getName();


            List<PsiElement> list = create(project, packageName, name);
//            String tomcatPathFromSettings = settings.getTomcatPath();
//            PsiFile fileA = PsiManager.getInstance(project).findFile(LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File("/test.txt")));
//            PsiFile fileB = PsiManager.getInstance(project).findFile(LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File("/rebel.xml")));


            return GotoRelatedItem.createItems(list, "group");

        } catch (Exception e) {
            return Collections.emptyList();
        }
//        return GotoRelatedItem.createItems(Arrays.asList(psiElement, psiElement), "group");
    }

    @NotNull
    private List<PsiElement> create(Project project, String packageName, String name) {
        List<String> candidates = getCandidates(packageName, name);
        List<PsiElement> list = new ArrayList<>();
        for (String candidate : candidates) {

            findAndAdd(list, project, candidate);
        }
        return list;
    }

    @NotNull
    @VisibleForTesting
    List<String> getCandidates(String packageName, String name) {
        List<String> candidates = new ArrayList<>();
        if (packageName.endsWith(".client") && !name.endsWith("Async.java")) {
            //client.Service -> server.ServiceImpl
            String clientToServer = clientToServer(name);
            if (clientToServer != null) {

                String fqn = packageName.substring(0, packageName.length() - ".client".length()) + ".server" + "." + clientToServer;
                candidates.add(fqn);
            }
            //client.Service -> client.ServiceAsync
            String clientToClientAsync = clientToClientAsync(name);
            if (clientToClientAsync != null) {

                String fqn = packageName.substring(0, packageName.length() - ".client".length()) + ".client" + "." + clientToClientAsync;
                candidates.add(fqn);
            }

        }

        if (packageName.endsWith(".client") && name.endsWith("Async.java")) {
            //client.ServiceAsync -> server.ServiceImpl

            String clientAsyncToServer = clientAsyncToServer(name);
            if (clientAsyncToServer != null) {

                String fqn = packageName.substring(0, packageName.length() - ".client".length()) + ".server" + "." + clientAsyncToServer;
                candidates.add(fqn);
            }
            //client.ServiceAsync -> client.Service
            String clientAsyncToClient = clientAsyncToClient(name);
            if (clientAsyncToClient != null) {

                String fqn = packageName.substring(0, packageName.length() - ".client".length()) + ".client" + "." + clientAsyncToClient;
                candidates.add(fqn);
            }

        }
        if (packageName.endsWith(".server")) {
            //service.ServiceImpl -> client.Service
            String serverToClient = serverToClient(name);
            if (serverToClient != null) {

                String fqn = packageName.substring(0, packageName.length() - ".server".length()) + ".client" + "." + serverToClient;
                candidates.add(fqn);
            }
            //service.ServiceImpl -> client.ServiceAsync
            String serverToClientAsync = serverToClientAsync(name);
            if (serverToClientAsync != null) {

                String fqn = packageName.substring(0, packageName.length() - ".server".length()) + ".client" + "." + serverToClientAsync;
                candidates.add(fqn);

            }

        }
        return candidates;
    }

    private void findAndAdd(List<PsiElement> list, Project project, String fqn) {
        PsiClass[] classes = JavaPsiFacade.getInstance(project).findClasses(fqn, GlobalSearchScope.allScope(project));
        for (PsiClass aClass : classes) {
            list.add(aClass.getNavigationElement());
        }
    }

    @Nullable
    private String serverToClient(String name) {
        return replaceEnd(name, "Impl.java", "");

    }

    @Nullable
    private String serverToClientAsync(String name) {
        return replaceEnd(name, "Impl.java", "Async");
    }

    @Nullable
    private String replaceEnd(String name, String oldSuffix, String newSuffix) {
        String plain = removeFromEnd(name, oldSuffix);
        if (plain != null) {
            return plain + newSuffix;
        }
        return null;
    }

    private String removeFromEnd(String name, String suffix) {
        if (name.endsWith(suffix)) {
            return name.substring(0, name.length() - suffix.length());
        }
        return null;
    }

    @Nullable
    private String clientToServer(String name) {
        return replaceEnd(name, ".java", "Impl");

    }

    @Nullable
    private String clientAsyncToServer(String name) {
        return replaceEnd(name, "Async.java", "Impl");

    }

    @Nullable
    private String clientAsyncToClient(String name) {
        return replaceEnd(name, "Async.java", "");

    }

    @Nullable
    private String clientToClientAsync(String name) {
        return replaceEnd(name, ".java", "As`ync");

    }


}