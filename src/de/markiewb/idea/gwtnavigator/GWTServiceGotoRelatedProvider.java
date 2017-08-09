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
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.MethodSignature;
import com.intellij.psi.util.MethodSignatureUtil;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GWTServiceGotoRelatedProvider extends GotoRelatedProvider {

    @Override
    public List<? extends GotoRelatedItem> getItems(@NotNull PsiElement psiElement) {


        Project project = psiElement.getProject();
        try {

            if (!(psiElement.getContainingFile() instanceof PsiJavaFileImpl)) {
                return Collections.emptyList();
            }
            PsiJavaFileImpl original = (PsiJavaFileImpl) psiElement.getContainingFile();
            String packageName = original.getPackageName();
            String className = removeFromEnd(original.getName(), ".java");

            List<PsiClass> targetClasses = createItems(project, new GWTServiceRelatedProvider().getCandidates(packageName, className));


            PsiMethod surroundingMethod = PsiTreeUtil.getParentOfType(psiElement, PsiMethod.class);
            List<PsiElement> result = new ArrayList<>();
            if (surroundingMethod != null) {
                //method has been selected, so jump to related method
                for (PsiClass aClass : targetClasses) {
                    List<PsiMethod> psiMethods = Arrays.asList(aClass.getMethods());

                    boolean foundMethod = false;
                    for (PsiMethod psiMethod : psiMethods) {

                        MethodSignature sigNew = psiMethod.getSignature(PsiSubstitutor.EMPTY);
                        MethodSignature sigOrig = surroundingMethod.getSignature(PsiSubstitutor.EMPTY);

                        //compare for same signature but without last AsyncCallBack parameter greet(String) with greet(String, AsyncCallback)
                        MethodSignature sigNewWithoutAsync = MethodSignatureUtil.createMethodSignature(sigNew.getName(), removeAsyncCallback(sigNew.getParameterTypes(), project), sigNew.getTypeParameters(), sigNew.getSubstitutor());
                        MethodSignature sigOrigWithoutAsync = MethodSignatureUtil.createMethodSignature(sigOrig.getName(), removeAsyncCallback(sigOrig.getParameterTypes(), project), sigOrig.getTypeParameters(), sigOrig.getSubstitutor());
                        if (MethodSignatureUtil.areSignaturesEqual(sigNewWithoutAsync, sigOrigWithoutAsync)) {
                            result.add(psiMethod);
                            foundMethod = true;
                            break;
                        }
                    }
                    if (!foundMethod) {
                        result.add(aClass);
                    }
                }
            } else {
                //no method has been selected, so jump to file
                result.addAll(targetClasses);
            }

            return GotoRelatedItem.createItems(result, "Google Web Toolkit");

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private PsiType[] removeAsyncCallback(PsiType[] parameterTypes, Project project) {
        List<PsiType> psiTypes = Arrays.asList(parameterTypes);
        if (psiTypes.size() >= 1) {

            PsiType last = psiTypes.get(psiTypes.size() - 1);
            PsiClassType typeByName = PsiType.getTypeByName("com.google.gwt.user.client.rpc.AsyncCallback", project, GlobalSearchScope.allScope(project));
            if (last.isAssignableFrom(typeByName)) {
                return psiTypes.subList(0, psiTypes.size() - 1).toArray(new PsiType[0]);
            }
        }
        return parameterTypes;
    }

    @NotNull
    private List<PsiClass> createItems(Project project, List<String> fqns) {

        List<PsiClass> list = new ArrayList<>();
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

    private List<PsiClass> findAndAdd(Project project, String fqn) {
        List<PsiClass> list = new ArrayList<>();

        PsiClass[] classes = JavaPsiFacade.getInstance(project).findClasses(fqn, GlobalSearchScope.allScope(project));
        for (PsiClass aClass : classes) {
            list.add(aClass);
        }
        return list;
    }


}