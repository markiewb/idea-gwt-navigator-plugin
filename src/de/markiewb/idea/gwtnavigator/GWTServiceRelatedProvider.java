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
        if (name.endsWith("Impl.java")) {
            String substring = name.substring(0, name.length() - "Impl.java".length());
            return substring;
        }
        return null;
    }

    @Nullable
    private String serverToClientAsync(String name) {
        if (name.endsWith("Impl.java")) {
            String substring = name.substring(0, name.length() - "Impl.java".length());
            return substring + "Async";
        }
        return null;
    }

    @Nullable
    private String clientToServer(String name) {
        if (name.endsWith(".java")) {
            String substring = name.substring(0, name.length() - ".java".length());
            return substring + "Impl";
        }
        return null;
    }

    @Nullable
    private String clientAsyncToServer(String name) {
        if (name.endsWith("Async.java")) {
            String substring = name.substring(0, name.length() - "Async.java".length());
            return substring + "Impl";
        }
        return null;
    }

    @Nullable
    private String clientAsyncToClient(String name) {
        if (name.endsWith("Async.java")) {
            String substring = name.substring(0, name.length() - "Async.java".length());
            return substring;
        }
        return null;
    }

    @Nullable
    private String clientToClientAsync(String name) {
        if (name.endsWith(".java")) {
            String substring = name.substring(0, name.length() - ".java".length());
            return substring + "Async";
        }
        return null;
    }


//    @NotNull
//    @Override
//    public List<? extends GotoRelatedItem> getItems(@NotNull DataContext context) {
//
//        Project project = context.getData(CommonDataKeys.PROJECT);
//        //Settings settings = ServiceManager.getService(project, Settings.class);
//
//        PsiFile file = context.getData(CommonDataKeys.PSI_FILE);
//
//        String fileName = file.getVirtualFile().getName();
//
//        //String remoteFileName = settings.getTomcatPath() + "/WEB-INF/classes/com/company/beanconfigs/" + fileName;
//        String remoteFileName ="";
//        VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(remoteFileName));
//        if (virtualFile != null) {
//
//            PsiFile remoteFile = PsiManager.getInstance(project).findFile(virtualFile);
////            ContentDiffRequest req = DiffRequestFactory.getInstance().createFromFiles(project, file1, file2);
////            DiffManager.getInstance().showDiff(project, req);
//
//            List<GotoRelatedItem> items = new ArrayList<>();
////            items.addAll(GotoRelatedItem.createItems(Arrays.asList(remoteFile), "Remote file"));
//            items.add(new GotoRelatedItem(remoteFile) {
//                @Override
//                public void navigate() {
//                    ContentDiffRequest req = DiffRequestFactory.getInstance().createFromFiles(project, file.getVirtualFile(), remoteFile.getVirtualFile());
//                    DiffManager.getInstance().showDiff(project, req);
//                }
//
//                @Nullable
//                @Override
//                public String getCustomName() {
//                    return "Diff remote " + remoteFile.getName();
//                }
//            });
//            return items;
//        }
//        return Collections.emptyList();
//
}