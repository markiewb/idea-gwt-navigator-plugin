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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringHelper {
    @Nullable
    public static String removeFromEnd(@NotNull String name, @NotNull String suffix) {
        if (name.endsWith(suffix)) {
            return name.substring(0, name.length() - suffix.length());
        }
        return null;
    }

    @Nullable
    public static String replaceEnd(String name, String oldSuffix, String newSuffix) {
        String plain = removeFromEnd(name, oldSuffix);
        if (plain != null) {
            return plain + newSuffix;
        }
        return null;
    }
}
