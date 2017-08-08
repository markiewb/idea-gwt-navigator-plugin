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

import com.google.common.annotations.VisibleForTesting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class GWTServiceRelatedProvider {
    @NotNull
    @VisibleForTesting
    List<String> getCandidates(String packageName, String name) {
        List<String> candidates = new ArrayList<>();
        if (packageName.endsWith(".client") && !name.endsWith("Async")) {

            //client.Service -> server.ServiceImpl
            String clientToServer = replaceEnd(name, "", "Impl");
            if (clientToServer != null) {
                candidates.add(replaceWithinPackageName(packageName, ".client", ".server", clientToServer));
            }

            //client.Service -> client.ServiceAsync
            String clientToClientAsync = replaceEnd(name, "", "Async");
            if (clientToClientAsync != null) {
                candidates.add(replaceWithinPackageName(packageName, ".client", ".client", clientToClientAsync));
            }
        }

        if (packageName.endsWith(".client") && name.endsWith("Async")) {

            //client.ServiceAsync -> server.ServiceImpl
            String clientAsyncToServer = replaceEnd(name, "Async", "Impl");
            if (clientAsyncToServer != null) {
                candidates.add(replaceWithinPackageName(packageName, ".client", ".server", clientAsyncToServer));
            }

            //client.ServiceAsync -> client.Service
            String clientAsyncToClient = replaceEnd(name, "Async", "");
            if (clientAsyncToClient != null) {
                candidates.add(replaceWithinPackageName(packageName, ".client", ".client", clientAsyncToClient));
            }

        }
        if (packageName.endsWith(".server")) {

            //service.ServiceImpl -> client.Service
            String serverToClient = replaceEnd(name, "Impl", "");
            if (serverToClient != null) {

                candidates.add(replaceWithinPackageName(packageName, ".server", ".client", serverToClient));
            }

            //service.ServiceImpl -> client.ServiceAsync
            String serverToClientAsync = replaceEnd(name, "Impl", "Async");
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
}
