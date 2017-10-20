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

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.psi.util.InheritanceUtil.getSuperClasses;
import static java.util.Collections.emptyList;

public class PSIHelper {

    @NotNull
    static Collection<PsiClass> findPsiClassByFQN(@NotNull Project project, @NotNull Collection<String> fqns) {

        List<PsiClass> list = new ArrayList<>();
        for (String fqn : fqns) {

            list.addAll(findPsiClassByFQN(project, fqn));
        }
        return list;
    }

    @NotNull
    static List<PsiClass> findPsiClassByFQN(@NotNull Project project, @Nullable String fqn) {
        if (fqn == null) {
            return emptyList();
        }
        List<PsiClass> list = new ArrayList<>();

        PsiClass[] classes = JavaPsiFacade.getInstance(project).findClasses(fqn, GlobalSearchScope.allScope(project));
        list.addAll(Arrays.asList(classes));
        return list;
    }

    public static boolean hasSuperClassRecursive(Project project, PsiClass currentClass, String fqnToFind) {
        //find super classes
        Set<PsiClass> supers = new HashSet<>();
        getSuperClasses(currentClass, supers, false);

        //check for type
        PsiClass[] clazzesToFind = JavaPsiFacade.getInstance(project).findClasses(fqnToFind, GlobalSearchScope.allScope(project));
        for (PsiClass allSuper : supers) {
            for (PsiClass clazzToFind : clazzesToFind) {

                if (allSuper.isInheritor(clazzToFind, true)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static Set<PsiClass> getInterfacesRecursive(Project project, PsiClass currentClass) {
        //find super classes
        Set<PsiClass> supers = new HashSet<>();
        getSuperClasses(currentClass, supers, false);

        Set<PsiClass> result = new HashSet<>();

        Queue<PsiClass> queue = new LinkedList<>();
        queue.addAll(Arrays.asList(currentClass.getInterfaces()));
        for (PsiClass x : supers) {
            queue.addAll(Arrays.asList(x.getInterfaces()));
        }

        while (!queue.isEmpty()) {
            PsiClass c = queue.poll();
            if (!result.contains(c)) {
                result.add(c);

                queue.addAll(Arrays.asList(c.getInterfaces()));
            }
        }
        return result;
    }
}
