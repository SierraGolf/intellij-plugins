package org.angularjs;

import com.intellij.codeInsight.TargetElementEvaluator;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.impl.JSTextReference;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSTargetElementEvaluator implements TargetElementEvaluator {
  @Override
  public boolean includeSelfInGotoImplementation(@NotNull PsiElement element) {
    return false;
  }

  @Nullable
  @Override
  public PsiElement getElementByReference(@NotNull PsiReference ref, int flags) {
    if (ref instanceof JSTextReference) {
      final PsiElement element = ref.getElement();
      final JSCallExpression call = PsiTreeUtil.getParentOfType(element, JSCallExpression.class);
      final JSExpression expression = call != null ? call.getMethodExpression() : null;
      if (expression instanceof JSReferenceExpression) {
        JSReferenceExpression callee = (JSReferenceExpression)expression;
        JSExpression qualifier = callee.getQualifier();

        if (qualifier != null && "directive".equals(callee.getReferencedName()) &&
            AngularIndexUtil.hasAngularJS(element.getProject())) {
          return element;
        }
      }
    }
    return null;
  }
}
