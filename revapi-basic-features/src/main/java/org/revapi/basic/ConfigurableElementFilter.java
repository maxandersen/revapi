/*
 * Copyright 2014 Lukas Krejci
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package org.revapi.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jboss.dmr.ModelNode;
import org.revapi.AnalysisContext;
import org.revapi.Element;
import org.revapi.ElementFilter;

/**
 * An element filter that can filter out elements based on matching their full human readable representations.
 * The configuration looks like follows:
 * <pre><code>
 * {
 *      "revapi" : {
 *          "filter" : {
 *              "include" : ["REGEX_ON_ELEMENT_FULL_REPRESENTATIONS", "ANOTHER_REGEX_ON_ELEMENT_FULL_REPRESENTATIONS"],
 *              "exclude" : ["REGEX_ON_ELEMENT_FULL_REPRESENTATIONS", "ANOTHER_REGEX_ON_ELEMENT_FULL_REPRESENTATIONS"]
 *          }
 *      }
 * }
 * </code></pre>
 * <p/>
 * The "suffixes" in the property names (i.e. the part after "revapi.filter.(in|ex)clude") are optional and are
 * supported so that it is possible to have multiple include filters defined in a properties file.
 * <p/>
 * If no include or exclude filters are defined, everything is included. If at least 1 include filter is defined, only
 * elements matching it are included. Out of the included elements, some may be further excluded by the exclude
 * filters.
 *
 * @author Lukas Krejci
 * @since 0.1
 */
public class ConfigurableElementFilter implements ElementFilter {
    private final List<Pattern> includes = new ArrayList<>();
    private final List<Pattern> excludes = new ArrayList<>();

    @Override
    public void initialize(@Nonnull AnalysisContext analysisContext) {
        ModelNode root = analysisContext.getConfiguration().get("revapi", "filter");
        for (ModelNode inc : root.get("includes").asList()) {
            includes.add(Pattern.compile(inc.asString()));
        }

        for (ModelNode exc : root.get("excludes").asList()) {
            excludes.add(Pattern.compile(exc.asString()));
        }
    }

    @Override
    public boolean applies(@Nullable Element element) {
        boolean include = true;
        String representation = element == null ? "" : element.getFullHumanReadableString();

        if (!includes.isEmpty()) {
            include = false;
            for (Pattern p : includes) {
                if (p.matcher(representation).matches()) {
                    include = true;
                    break;
                }
            }
        }

        if (include) {
            for (Pattern p : excludes) {
                if (p.matcher(representation).matches()) {
                    include = false;
                    break;
                }
            }
        }

        return include;
    }

    @Override
    public boolean shouldDescendInto(@Nullable Object element) {
        return true;
    }

    @Override
    public void close() {
    }
}
