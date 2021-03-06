package com.jetbrains.lang.dart.util;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.ide.index.DartLibraryIndex;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.resolve.DartResolveProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class DartPsiImplUtil {

  private static final String TRIPLE_APOS = "'''";
  private static final String TRIPLE_QUOTE = "\"\"\"";
  private static final String APOS = "'";
  private static final String QUOTE = "\"";
  private static final String R_TRIPLE_APOS = "r'''";
  private static final String R_TRIPLE_QUOTE = "r\"\"\"";
  private static final String R_APOS = "r'";
  private static final String R_QUOTE = "r\"";

  @NotNull
  public static String getUriString(@NotNull final DartUriBasedDirective uriBasedDirective) {
    return unquoteDartString(uriBasedDirective.getUriElement().getText());
  }

  public static String unquoteDartString(@NotNull final String quotedDartString) {
    // r'''dart:core'''
    // """package:angular/angular.dart"""
    // "../foo/bar.dart"
    // also can be not closed string when completing for example import '<caret>
    if (quotedDartString.startsWith(TRIPLE_APOS)) {
      return StringUtil.trimEnd(quotedDartString.substring(TRIPLE_APOS.length()), TRIPLE_APOS);
    }
    if (quotedDartString.startsWith(TRIPLE_QUOTE)) {
      return StringUtil.trimEnd(quotedDartString.substring(TRIPLE_QUOTE.length()), TRIPLE_QUOTE);
    }
    if (quotedDartString.startsWith(APOS)) {
      return StringUtil.trimEnd(quotedDartString.substring(APOS.length()), APOS);
    }
    if (quotedDartString.startsWith(QUOTE)) {
      return StringUtil.trimEnd(quotedDartString.substring(QUOTE.length()), QUOTE);
    }
    if (quotedDartString.startsWith(R_TRIPLE_APOS)) {
      return StringUtil.trimEnd(quotedDartString.substring(R_TRIPLE_APOS.length()), TRIPLE_APOS);
    }
    if (quotedDartString.startsWith(R_TRIPLE_QUOTE)) {
      return StringUtil.trimEnd(quotedDartString.substring(R_TRIPLE_QUOTE.length()), TRIPLE_QUOTE);
    }
    if (quotedDartString.startsWith(R_APOS)) {
      return StringUtil.trimEnd(quotedDartString.substring(R_APOS.length()), APOS);
    }
    if (quotedDartString.startsWith(R_QUOTE)) {
      return StringUtil.trimEnd(quotedDartString.substring(R_QUOTE.length()), QUOTE);
    }

    return quotedDartString;
  }

  public static int getUriStringOffset(@NotNull final DartUriBasedDirective uriBasedDirective) {
    // similar to #unquoteDartString()
    final String quotedDartString = uriBasedDirective.getUriElement().getText();
    if (quotedDartString.startsWith(TRIPLE_APOS) || quotedDartString.startsWith(TRIPLE_QUOTE)) {
      return TRIPLE_QUOTE.length();
    }
    if (quotedDartString.startsWith(APOS) || quotedDartString.startsWith(QUOTE)) {
      return QUOTE.length();
    }
    if (quotedDartString.startsWith(R_TRIPLE_APOS) || quotedDartString.startsWith(R_TRIPLE_QUOTE)) {
      return R_TRIPLE_QUOTE.length();
    }
    if (quotedDartString.startsWith(R_APOS) || quotedDartString.startsWith(R_QUOTE)) {
      return R_QUOTE.length();
    }
    return 0;
  }

  @NotNull
  public static String getLibraryName(@NotNull DartLibraryStatement libraryStatement) {
    final DartQualifiedComponentName componentName = libraryStatement.getQualifiedComponentName();
    return StringUtil.notNullize(componentName.getName());
  }

  @NotNull
  public static String getLibraryName(@NotNull DartPartOfStatement importStatement) {
    final DartLibraryId expression = importStatement.getLibraryId();
    return FileUtil.toSystemIndependentName(StringUtil.unquoteString(expression.getText()));
  }

  @NotNull
  public static List<DartMetadata> getMetadataList(@NotNull DartLabel element) {
    return Collections.emptyList();
  }

  @NotNull
  public static List<DartMetadata> getMetadataList(@NotNull DartEnumConstantDeclaration element) {
    return Collections.emptyList();
  }

  @NotNull
  public static List<DartMetadata> getMetadataList(@NotNull DartVarDeclarationListPart element) {
    return Collections.emptyList();
  }

  @Nullable
  public static PsiElement resolveReference(@NotNull DartType dartType) {
    final DartExpression expression = dartType.getReferenceExpression();
    final String typeName = expression.getText();
    if (typeName.indexOf('.') != -1) {
      return ((DartReference)expression).resolve();
    }
    List<DartComponentName> result = new ArrayList<DartComponentName>();
    final DartResolveProcessor dartResolveProcessor = new DartResolveProcessor(result, typeName);

    final VirtualFile virtualFile = DartResolveUtil.getRealVirtualFile(dartType.getContainingFile());
    if (virtualFile != null) {
      DartResolveUtil.processTopLevelDeclarations(dartType, dartResolveProcessor, virtualFile, typeName);
    }
    // find type parameter
    if (result.isEmpty()) {
      PsiTreeUtil.treeWalkUp(dartResolveProcessor, dartType, null, ResolveState.initial());
      for (Iterator<DartComponentName> iterator = result.iterator(); iterator.hasNext(); ) {
        if (!(iterator.next().getParent() instanceof DartTypeParameter)) {
          iterator.remove();
        }
      }
    }
    // global
    if (result.isEmpty()) {
      final List<VirtualFile> libraryFile = DartResolveUtil.findLibrary(dartType.getContainingFile());
      DartResolveUtil.processTopLevelDeclarations(dartType, dartResolveProcessor, libraryFile, typeName);
    }
    // dart:core
    if (result.isEmpty()) {
      final List<VirtualFile> libraryFile = DartLibraryIndex.findLibraryClass(dartType, "dart:core");
      DartResolveUtil.processTopLevelDeclarations(dartType, dartResolveProcessor, libraryFile, typeName);
    }
    return result.isEmpty() ? null : result.iterator().next();
  }

  @Nullable
  public static DartComponentName findComponentName(final @NotNull DartNormalFormalParameter normalFormalParameter) {
    final DartFunctionSignature functionDeclaration = normalFormalParameter.getFunctionSignature();
    final DartFieldFormalParameter fieldFormalParameter = normalFormalParameter.getFieldFormalParameter();
    final DartSimpleFormalParameter simpleFormalParameter = normalFormalParameter.getSimpleFormalParameter();

    if (functionDeclaration != null) return functionDeclaration.getComponentName();
    if (fieldFormalParameter != null) return null;
    if (simpleFormalParameter != null) return simpleFormalParameter.getComponentName();

    assert false : normalFormalParameter.getText();
    return null;
  }

  public static DartExpression getParameterReferenceExpression(DartNamedArgument argument) {
    return PsiTreeUtil.getChildOfType(argument, DartExpression.class);
  }

  public static DartExpression getExpression(DartNamedArgument argument) {
    final DartExpression[] expressions = PsiTreeUtil.getChildrenOfType(argument, DartExpression.class);
    return expressions != null && expressions.length > 1 ? expressions[expressions.length - 1] : null;
  }

  @Nullable
  public static DartBlock getBlock(DartFunctionBody functionBody) {
    return PsiTreeUtil.getChildOfType(functionBody, DartBlock.class);
  }
}
