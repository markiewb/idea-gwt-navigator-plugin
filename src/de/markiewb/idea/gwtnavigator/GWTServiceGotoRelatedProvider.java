/**
 * Copyright 2017 markiewb
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package de.markiewb.idea.gwtnavigator;

import com.intellij.navigation.GotoRelatedItem;
import com.intellij.navigation.GotoRelatedProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GWTServiceGotoRelatedProvider extends GotoRelatedProvider {

    @Override
    public List<? extends GotoRelatedItem> getItems(@NotNull PsiElement psiElement) {


        Project project = psiElement.getProject();
        try {

            if (!(psiElement.getContainingFile() instanceof PsiJavaFileImpl)) {
                return null;
            }
            PsiJavaFileImpl original = (PsiJavaFileImpl) psiElement.getContainingFile();
            String packageName = original.getPackageName();
            String className = removeFromEnd(original.getName(), ".java");

            List<PsiElement> list = createItems(project, new GWTServiceRelatedProvider().getCandidates(packageName, className));
            return GotoRelatedItem.createItems(list, "Google Web Toolkit");

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @NotNull
    private List<PsiElement> createItems(Project project, List<String> fqns) {

        List<PsiElement> list = new ArrayList<>();
        for (String fqn : fqns) {

            list.addAll(findAndAdd(project, fqn));
        }
        return list;
    }

    private String removeFromEnd(String name, String suffix) {
        if (name.endsWith(suffix)) {
            return name.substring(0, name.length() - suffix.length());
        }
        return null;
    }

    private List<PsiElement> findAndAdd(Project project, String fqn) {
        List<PsiElement> list = new ArrayList<>();

        PsiClass[] classes = JavaPsiFacade.getInstance(project).findClasses(fqn, GlobalSearchScope.allScope(project));
        for (PsiClass aClass : classes) {
            list.add(aClass.getNavigationElement());
        }
        return list;
    }


}