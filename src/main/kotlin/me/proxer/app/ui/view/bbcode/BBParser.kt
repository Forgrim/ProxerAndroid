package me.proxer.app.ui.view.bbcode

import me.proxer.app.ui.view.bbcode.prototype.AttachmentPrototype
import me.proxer.app.ui.view.bbcode.prototype.AutoClosingPrototype
import me.proxer.app.ui.view.bbcode.prototype.BBPrototype.Companion.REGEX_OPTIONS
import me.proxer.app.ui.view.bbcode.prototype.BoldPrototype
import me.proxer.app.ui.view.bbcode.prototype.CenterPrototype
import me.proxer.app.ui.view.bbcode.prototype.CodePrototype
import me.proxer.app.ui.view.bbcode.prototype.ColorPrototype
import me.proxer.app.ui.view.bbcode.prototype.DividerPrototype
import me.proxer.app.ui.view.bbcode.prototype.HidePrototype
import me.proxer.app.ui.view.bbcode.prototype.ImagePrototype
import me.proxer.app.ui.view.bbcode.prototype.ItalicPrototype
import me.proxer.app.ui.view.bbcode.prototype.LeftPrototype
import me.proxer.app.ui.view.bbcode.prototype.ListItemPrototype
import me.proxer.app.ui.view.bbcode.prototype.MapPrototype
import me.proxer.app.ui.view.bbcode.prototype.OrderedListPrototype
import me.proxer.app.ui.view.bbcode.prototype.QuotePrototype
import me.proxer.app.ui.view.bbcode.prototype.RightPrototype
import me.proxer.app.ui.view.bbcode.prototype.RootPrototype
import me.proxer.app.ui.view.bbcode.prototype.SizePrototype
import me.proxer.app.ui.view.bbcode.prototype.SpoilerPrototype
import me.proxer.app.ui.view.bbcode.prototype.StrikethroughPrototype
import me.proxer.app.ui.view.bbcode.prototype.SubscriptPrototype
import me.proxer.app.ui.view.bbcode.prototype.SuperscriptPrototype
import me.proxer.app.ui.view.bbcode.prototype.TableCellPrototype
import me.proxer.app.ui.view.bbcode.prototype.TablePrototype
import me.proxer.app.ui.view.bbcode.prototype.TableRowPrototype
import me.proxer.app.ui.view.bbcode.prototype.TextPrototype
import me.proxer.app.ui.view.bbcode.prototype.UnderlinePrototype
import me.proxer.app.ui.view.bbcode.prototype.UnorderedListPrototype
import me.proxer.app.ui.view.bbcode.prototype.UrlPrototype
import me.proxer.app.ui.view.bbcode.prototype.VideoPrototype
import java.util.regex.Pattern.quote

/**
 * @author Ruben Gees
 */
object BBParser {

    private val prototypes = arrayOf(BoldPrototype, ItalicPrototype, UnderlinePrototype, StrikethroughPrototype,
            SizePrototype, ColorPrototype, LeftPrototype, CenterPrototype, RightPrototype, SpoilerPrototype,
            QuotePrototype, UrlPrototype, ImagePrototype, DividerPrototype, VideoPrototype, SuperscriptPrototype,
            SubscriptPrototype, TablePrototype, TableRowPrototype, TableCellPrototype, CodePrototype, HidePrototype,
            UnorderedListPrototype, OrderedListPrototype, ListItemPrototype, MapPrototype, AttachmentPrototype)

    private val prototypeRegex
        get() = prototypes.joinToString("|") {
            when (it.canHaveChildren) {
                true -> it.startRegex.pattern + "|" + it.endRegex.pattern
                false -> it.startRegex.pattern
            }
        }

    private val regex = Regex("${quote("[")}(($prototypeRegex)?)${quote("]")}", REGEX_OPTIONS)

    fun parse(input: String): BBTree {
        val trimmedInput = input.trim()
        val result = BBTree(RootPrototype, null)
        val parts = regex.findAll(trimmedInput)

        var currentTree = result
        var currentPosition = 0

        parts.forEach {
            val part = it.groupValues[1].trim()

            if (it.range.first > currentPosition) {
                val startString = trimmedInput.substring(currentPosition, it.range.first)

                currentTree.children.add(TextPrototype.construct(startString, currentTree))
            }

            if (currentTree.endsWith(part)) {
                currentTree.isFinished = true

                currentTree = findNextUnfinishedTree(currentTree)
            } else {
                var prototypeFound = false

                for (prototype in prototypes) {
                    val newTree = prototype.fromCode(it.groupValues[1], currentTree)

                    if (newTree != null) {
                        currentTree.children.add(newTree)
                        prototypeFound = true

                        if (prototype.canHaveChildren) currentTree = newTree

                        break
                    }
                }

                // If nothing found assume a user error and look for a fitting end tag in the existing tree.
                if (!prototypeFound) {
                    val fittingTree = findFittingTree(currentTree, part)

                    if (fittingTree != null) {
                        fittingTree.isFinished = true

                        if (fittingTree.prototype is AutoClosingPrototype) {
                            currentTree = fittingTree.parent ?: throw IllegalArgumentException("parent is null")
                        }
                    } else {
                        val unknownString = trimmedInput.substring(it.range.first, it.range.endInclusive + 1)

                        currentTree.children.add(TextPrototype.construct(unknownString, currentTree))
                    }
                }
            }

            currentPosition = it.range.endInclusive + 1
        }

        if (currentPosition < trimmedInput.length) {
            val endString = trimmedInput.substring(currentPosition, trimmedInput.length)

            currentTree.children.add(TextPrototype.construct(endString, currentTree))
        }

        return result
    }

    private fun findFittingTree(tree: BBTree, endTag: String): BBTree? {
        var currentTree = tree.parent

        while (true) {
            if (currentTree?.endsWith(endTag) == true && !currentTree.isFinished) {
                return currentTree
            } else if (currentTree?.parent == null) {
                return null
            }

            currentTree = currentTree.parent
        }
    }

    private fun findNextUnfinishedTree(tree: BBTree): BBTree {
        var currentTree = tree

        while (true) {
            if (!currentTree.isFinished) {
                return currentTree
            } else {
                currentTree = currentTree.parent ?: throw IllegalStateException("No unfinished tree found")
            }
        }
    }
}
