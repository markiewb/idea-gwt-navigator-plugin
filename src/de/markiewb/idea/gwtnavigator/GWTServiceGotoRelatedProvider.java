/*
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
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.psi.util.MethodSignature;
import com.intellij.psi.util.MethodSignatureUtil;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.*;

public class GWTServiceGotoRelatedProvider extends GotoRelatedProvider {


    /**
     * Information about GWT-conventions:
     * <p>
     * <a href="http://mangstacular.blogspot.de/2011/12/gwt-rpc-series-synchronous-and.html">http://mangstacular.blogspot.de/2011/12/gwt-rpc-series-synchronous-and.html</a>
     * <a href="https://dzone.com/articles/making-gwt-remote-procedure-ca">https://dzone.com/articles/making-gwt-remote-procedure-ca</a>
     * </p>
     * Algorithm in German:
     * <pre>
     * Helper(Interface)
     *  Wird Typ com.google.gwt.user.client.rpc.RemoteService extended? (nicht rekursiv) -
     *  Wenn ja, -> Interface gefunden
     *  Wenn ja, dann das Async-Interface im gleichen Package suchen  -> Async gefunden
     *  Wenn ja, dann alle Implementationen (Typ Klasse) des Interfaces finden -> Impl gefunden
     *
     * falls Interface
     *  Ergebnis = Helper(interface)
     *  Interface aus Ergebnis entfernen
     *
     * falls AsyncInterface
     *  Das sync-Interface im gleichen Package suchen
     *  Ergebnis = Helper (Async2Sync) und Async aus Ergebnis entfernen
     *
     * falls Impl
     *  wird com.google.gwt.user.server.rpc.RemoteServiceServlet implementiert? (nicht rekursiv)
     *  Ergebnis = Helper (alleImplementiertenInterfacesDerKlasse) und Impl aus Ergebnis entfernen
     *
     *
     * </pre>
     */
    @NotNull
    @Override
    public List<? extends GotoRelatedItem> getItems(@NotNull PsiElement psiElement) {

        Project project = psiElement.getProject();
        try {

            if (!(psiElement.getContainingFile() instanceof PsiJavaFileImpl)) {
                return Collections.emptyList();
            }


            PsiClass currentClass = PsiTreeUtil.getParentOfType(psiElement, PsiClass.class);
            if (currentClass == null) {
                return emptyList();
            }

            Set<PsiClass> targetClasses = new LinkedHashSet<>();
            targetClasses.addAll(getRelatedTypesForGWTSyncInterface(project, currentClass));
            targetClasses.addAll(getRelatedTypesForGWTASyncInterface(project, currentClass));
            targetClasses.addAll(getRelatedTypesForGWTImpl(project, currentClass));

            //sich selbst als Ergebnis entfernen
            targetClasses.remove(currentClass);


            List<PsiElement> methods = getCorrespondingMethods(psiElement, project, targetClasses);
            if (methods.isEmpty()) {
                return GotoRelatedItem.createItems(targetClasses, "Google Web Toolkit");
            } else {
                return GotoRelatedItem.createItems(methods, "Google Web Toolkit");
            }


        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @NotNull
    private List<PsiElement> getCorrespondingMethods(@NotNull PsiElement psiElement, @NotNull Project project, @NotNull Set<PsiClass> targetClasses) {
        PsiMethod surroundingMethod = PsiTreeUtil.getParentOfType(psiElement, PsiMethod.class);
        if (surroundingMethod != null) {
            List<PsiElement> result = new ArrayList<>();
            //method has been selected, so jump to related method
            for (PsiClass aClass : targetClasses) {
                List<PsiMethod> psiMethods = asList(aClass.getMethods());

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
            return result;
        } else {
            //no method has been selected, so jump to file
            return new ArrayList<>(targetClasses);
        }
    }

    private Collection<? extends PsiClass> getRelatedTypesForGWTImpl(Project project, PsiClass currentClass) {
        // wird com.google.gwt.user.server.rpc.RemoteServiceServlet implementiert? (rekursiv)
        // Ergebnis = Helper (alleImplementiertenInterfacesDerKlasse) und Impl aus Ergebnis entfernen

        if (!currentClass.isInterface()) {
            PsiClass[] supers = currentClass.getSupers();
            boolean isRemoteServiceServlet = stream(supers).anyMatch(x -> "com.google.gwt.user.server.rpc.RemoteServiceServlet".equals(x.getQualifiedName()));
            if (isRemoteServiceServlet) {
                Set<PsiClass> results = new LinkedHashSet<>();

                stream(currentClass.getInterfaces()).forEach(x -> results.addAll(getRelatedTypesForGWTSyncInterface(project, x)));
                return results;
            }
        }
        return emptySet();

    }

    @NotNull
    private Set<PsiClass> getRelatedTypesForGWTASyncInterface(@NotNull Project project, @NotNull PsiClass currentClass) {

        //      falls AsyncInterface
        //        Das sync-Interface im gleichen Package suchen
        //        Ergebnis = Helper (Async2Sync) und Async aus Ergebnis entfernen

        if (currentClass.isInterface()) {
            String qualifiedName = currentClass.getQualifiedName() != null ? currentClass.getQualifiedName() : "";
            if (qualifiedName.endsWith("Async")) {
                String syncQualifiedName = StringHelper.replaceEnd(qualifiedName, "Async", "");

                Set<PsiClass> result = new LinkedHashSet<>();
                for (PsiClass syncInterface : PSIHelper.findPsiClassByFQN(project, singleton(syncQualifiedName))) {
                    result.addAll(getRelatedTypesForGWTSyncInterface(project, syncInterface));
                }
                return result;
            }
        }
        return Collections.emptySet();
    }

    @NotNull
    private Set<PsiClass> getRelatedTypesForGWTSyncInterface(@NotNull Project project, @NotNull PsiClass currentClass) {
        if (currentClass.isInterface()) {
            PsiClass[] interfaces = currentClass.getInterfaces();
            boolean isRemoteServiceInterface = stream(interfaces).anyMatch(x -> "com.google.gwt.user.client.rpc.RemoteService".equals(x.getQualifiedName()));
            if (isRemoteServiceInterface) {
                Set<PsiClass> results = new LinkedHashSet<>();
                // Wird Typ com.google.gwt.user.client.rpc.RemoteService extended? (nicht rekursiv, RemoteService nicht selbst) -
                // Wenn ja, -> Interface gefunden
                results.add(currentClass);

                // Wenn ja, dann das Async-Interface im gleichen Package suchen  -> Async gefunden
                String asyncQualifiedName = StringHelper.replaceEnd(currentClass.getQualifiedName(), "", "Async");
                results.addAll(PSIHelper.findPsiClassByFQN(project, asyncQualifiedName));

                // Wenn ja, dann alle Implementationen (Typ Klasse) des Interfaces finden -> Impl gefunden
                Collection<PsiClass> all = ClassInheritorsSearch.search(currentClass, GlobalSearchScope.projectScope(project), true).findAll();
                // aber nur die wirklichen Servlets

                List<PsiClass> onlyServletImpls = all.stream().filter(this::isRemoteServlet).collect(Collectors.toList());

                results.addAll(onlyServletImpls);
                return results;
            }
        }
        return emptySet();
    }

    private boolean isRemoteServlet(@NotNull PsiClass x) {
        return stream(x.getSupers()).anyMatch(superClass -> "com.google.gwt.user.server.rpc.RemoteServiceServlet".equals(superClass.getQualifiedName()));
    }


    @NotNull
    private PsiType[] removeAsyncCallback(PsiType[] parameterTypes, @NotNull Project project) {
        List<PsiType> psiTypes = asList(parameterTypes);
        if (psiTypes.size() >= 1) {

            PsiType last = psiTypes.get(psiTypes.size() - 1);
            PsiClassType typeByName = PsiType.getTypeByName("com.google.gwt.user.client.rpc.AsyncCallback", project, GlobalSearchScope.allScope(project));
            if (last.isAssignableFrom(typeByName)) {
                return psiTypes.subList(0, psiTypes.size() - 1).toArray(new PsiType[0]);
            }
        }
        return parameterTypes;
    }


}