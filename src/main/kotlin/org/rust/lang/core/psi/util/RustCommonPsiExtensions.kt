package org.rust.lang.core.psi.util

import com.intellij.lang.ASTNode
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import org.rust.lang.core.psi.RustNamedElement
import org.rust.lang.core.psi.RustPatElement
import org.rust.lang.core.psi.visitors.RustRecursiveElementVisitor


/**
 * Common Rust's PSI-related extensions
 */

inline fun <reified T : PsiElement> PsiElement.parentOfType(strict: Boolean = true, minStartOffset: Int = -1): T? =
    PsiTreeUtil.getParentOfType(this, T::class.java, strict, minStartOffset)

inline fun <reified T : PsiElement> PsiElement.childOfType(strict: Boolean = true): T? =
    PsiTreeUtil.findChildOfType(this, T::class.java, strict)

inline fun <reified T : PsiElement> PsiElement.descendentsOfType(): Collection<T> =
    PsiTreeUtil.findChildrenOfType(this, T::class.java)

/**
 * Finds first sibling that is neither comment, nor whitespace before given element.
 */
fun PsiElement?.getPrevNonCommentSibling(): PsiElement? =
    PsiTreeUtil.skipSiblingsBackward(this, PsiWhiteSpace::class.java, PsiComment::class.java)

/**
 * Finds first sibling that is neither comment, nor whitespace after given element.
 */
fun PsiElement?.getNextNonCommentSibling(): PsiElement? =
    PsiTreeUtil.skipSiblingsForward(this, PsiWhiteSpace::class.java, PsiComment::class.java)


/**
 * Accounts for text-range relative to some ancestor (or the node itself) of the
 * given node
 */
fun PsiElement.rangeRelativeTo(ancestor: PsiElement): TextRange {
    check(ancestor.textRange.contains(textRange))
    return textRange.shiftRight(-ancestor.textRange.startOffset)
}

/**
 * Accounts for text-range relative to the parent of the element
 */
val PsiElement.parentRelativeRange: TextRange
    get() = rangeRelativeTo(parent)

fun ASTNode.containsEOL(): Boolean = textContains('\r') || textContains('\n')
fun PsiElement.containsEOL(): Boolean = textContains('\r') || textContains('\n')


/**
 * Returns module for this PsiElement.
 *
 * If the element is in a library, returns the module which depends on
 * the library.
 */
val PsiElement.module: Module?
    get() {
        // It's important to look the module for `containingFile` file
        // and not the element itself. Otherwise this will break for
        // elements in libraries.
        return ModuleUtilCore.findModuleForPsiElement(containingFile)
    }

/**
 * Checks whether this node contains [descendant] one
 */
fun PsiElement.contains(descendant: PsiElement?): Boolean {
    var p = descendant
    while (p != null) {
        if (p === this)
            return true

        p = p.parent
    }

    return false
}


/**
 * Composes tree-path from `this` node to the [other] (inclusive)
 */
fun PsiElement.pathTo(other: PsiElement): Iterable<PsiElement> {
    check(contains(other) || other.contains(this))

    // Check whether we're walking the tree up or down
    return if (other.contains(this))
        walkUp(this, other)
    else
        walkUp(other, this).reversed()
}

private fun walkUp(descendant: PsiElement, ancestor: PsiElement): Iterable<PsiElement> {
    val path = arrayListOf<PsiElement>()

    var p: PsiElement? = descendant
    while (p != null && p !== ancestor) {
        path.add(p)
        p = p.parent
    }

    path.add(ancestor)

    return path
}


/**
 * Extracts node's element type
 */
val PsiElement.elementType: IElementType
    get() = node.elementType