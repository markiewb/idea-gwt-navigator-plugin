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
        try {

            PsiJavaFileImpl original = (PsiJavaFileImpl) psiElement.getContainingFile();
            String packageName = original.getPackageName();
            String name = original.getName();


            List<PsiElement> list = create(project, packageName, name);
            return GotoRelatedItem.createItems(list, "Google Web Toolkit");

        } catch (Exception e) {
            return Collections.emptyList();
        }
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
                candidates.add(replaceWithinPackageName(packageName, ".client", ".server", clientToServer));
            }
            //client.Service -> client.ServiceAsync
            String clientToClientAsync = clientToClientAsync(name);
            if (clientToClientAsync != null) {
                candidates.add(replaceWithinPackageName(packageName, ".client", ".client", clientToClientAsync));
            }

        }

        if (packageName.endsWith(".client") && name.endsWith("Async.java")) {
            //client.ServiceAsync -> server.ServiceImpl

            String clientAsyncToServer = clientAsyncToServer(name);
            if (clientAsyncToServer != null) {
                candidates.add(replaceWithinPackageName(packageName, ".client", ".server", clientAsyncToServer));
            }
            //client.ServiceAsync -> client.Service
            String clientAsyncToClient = clientAsyncToClient(name);
            if (clientAsyncToClient != null) {
                candidates.add(replaceWithinPackageName(packageName, ".client", ".client", clientAsyncToClient));
            }

        }
        if (packageName.endsWith(".server")) {
            //service.ServiceImpl -> client.Service
            String serverToClient = serverToClient(name);
            if (serverToClient != null) {

                candidates.add(replaceWithinPackageName(packageName, ".server", ".client", serverToClient));
            }
            //service.ServiceImpl -> client.ServiceAsync
            String serverToClientAsync = serverToClientAsync(name);
            if (serverToClientAsync != null) {

                candidates.add(replaceWithinPackageName(packageName, ".server", ".client", serverToClientAsync));

            }

        }
        return candidates;
    }

    @NotNull
    private String replaceWithinPackageName(String packageName, String from, String to, String className) {
        return packageName.substring(0, packageName.length() - from.length()) + to + "." + className;
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
        return replaceEnd(name, ".java", "Async");

    }


}