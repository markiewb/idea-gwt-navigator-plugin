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
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.psi.PsiElement;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class GWTServiceProductionGotoRelatedProvider extends AbstractGWTServiceGotoRelatedProvider
{

    @Override
    protected List<? extends GotoRelatedItem> getGotoRelatedItems(Collection<? extends PsiElement> elements)
    {
        List<? extends PsiElement> collect = elements.stream().filter(x -> !ProjectFileIndex.SERVICE.getInstance(x.getProject()).isInTestSourceContent(x.getContainingFile().getVirtualFile())).collect(Collectors.toList());
        return GotoRelatedItem.createItems(collect, "Google Web Toolkit");
    }
}