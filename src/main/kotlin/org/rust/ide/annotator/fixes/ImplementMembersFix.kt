/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.ide.annotator.fixes

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.rust.ide.core.overrideImplement.generateTraitMembers
import org.rust.lang.core.psi.RsImplItem

/**
 * Adds unimplemented methods and associated types to an impl block
 */
class ImplementMembersFix(
    implBody: RsImplItem
) : LocalQuickFixAndIntentionActionOnPsiElement(implBody) {

    override fun getText(): String = "Implement methods"

    override fun getFamilyName(): String = text

    override fun startInWriteAction(): Boolean = false

    override fun invoke(
        project: Project,
        file: PsiFile,
        editor: Editor?,
        startElement: PsiElement,
        endElement: PsiElement
    ) {
        val impl = (startElement as RsImplItem)
        generateTraitMembers(impl, editor)
    }
}
