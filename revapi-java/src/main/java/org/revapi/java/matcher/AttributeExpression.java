/*
 * Copyright 2015-2017 Lukas Krejci
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 *
 */

package org.revapi.java.matcher;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import org.revapi.FilterMatch;
import org.revapi.java.spi.JavaAnnotationElement;
import org.revapi.java.spi.JavaModelElement;

/**
 * @author Lukas Krejci
 */
final class AttributeExpression implements MatchExpression {
    private final Function<AnnotationAttributeElement, FilterMatch> filter;
    private final boolean onlyExplicitValues;

    public AttributeExpression(@Nullable MatchExpression attributeNameMatch, @Nullable MatchExpression valueMatch,
                               boolean onlyExplicitValues) {
        this.onlyExplicitValues = onlyExplicitValues;

        Function<AnnotationAttributeElement, FilterMatch> nameFilter = attributeNameMatch == null
                ? __ -> FilterMatch.MATCHES
                : attributeNameMatch::matches;

        Function<AnnotationAttributeElement, Supplier<FilterMatch>> valueFilter = valueMatch == null
                ? __ -> () -> FilterMatch.MATCHES
                : e -> () -> valueMatch.matches(e);

        filter = e -> nameFilter.apply(e).and(valueFilter.apply(e));
    }

    @Override
    public FilterMatch matches(JavaModelElement element) {
        return FilterMatch.DOESNT_MATCH;
    }

    @Override
    public FilterMatch matches(JavaAnnotationElement annotation) {
        AnnotationMirror am = annotation.getAnnotation();
        Map<? extends ExecutableElement, ? extends AnnotationValue> attrs;

        if (onlyExplicitValues) {
            attrs = am.getElementValues();
        } else {
            attrs = annotation.getTypeEnvironment().getElementUtils()
                    .getElementValuesWithDefaults(am);
        }

        FilterMatch res = FilterMatch.DOESNT_MATCH;
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : attrs.entrySet()) {
            AnnotationAttributeElement el = new AnnotationAttributeElement(annotation, e.getKey(), e.getValue());

            res = res.or(filter.apply(el));

            if (res == FilterMatch.MATCHES) {
                return res;
            }
        }

        return res;
    }

    @Override
    public FilterMatch matches(AnnotationAttributeElement attribute) {
        return filter.apply(attribute);
    }

    @Override
    public FilterMatch matches(TypeParameterElement typeParameter) {
        return FilterMatch.DOESNT_MATCH;
    }

    @Override
    public FilterMatch matches(TypeMirror type) {
        return FilterMatch.DOESNT_MATCH;
    }
}
