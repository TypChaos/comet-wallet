package org.ergoplatform.ios.mosaik

import org.ergoplatform.ios.ui.*
import org.ergoplatform.mosaik.AppMosaikRuntime
import org.ergoplatform.mosaik.TreeElement
import org.ergoplatform.mosaik.model.MosaikManifest
import org.ergoplatform.mosaik.model.ui.layout.Column
import org.robovm.apple.coregraphics.CGRect
import org.robovm.apple.uikit.UIEdgeInsets
import org.robovm.apple.uikit.UIView
import java.util.*

class MosaikView(
    private val mosaikRuntime: AppMosaikRuntime
) : UIView(CGRect.Zero()) {

    private var root: ViewWithTreeElement? = null

    init {
        layoutMargins = UIEdgeInsets.Zero()
    }

    fun updateView() {
        val rootElement = mosaikRuntime.viewTree.content
        val setRootView: (UiViewHolder) -> Unit = { viewHolder ->
            val uiView = viewHolder.uiView
            addSubview(uiView)

            val positionAtTop = (viewHolder.treeElement.element is Column)

            val maxWidth = when (viewHolder.treeElement.viewTree.targetCanvasDimension) {
                MosaikManifest.CanvasDimension.COMPACT_WIDTH -> 500.0
                MosaikManifest.CanvasDimension.MEDIUM_WIDTH -> 720.0
                else -> 0.0
            }
            uiView.widthMatchesSuperview(maxWidth = maxWidth)
                .topToSuperview(canBeMore = !positionAtTop)
                .bottomToSuperview(canBeLess = true)

            if (maxWidth > 0)
                // maxWidth lowers the priority of left and right to match outer edges; this means
                // it can happen that the view stretches outside. We prevent this with additional
                // constraints
                uiView.leftToSuperview(canBeMore = true)
                    .rightToSuperview(canBeLess = true)

            if (!positionAtTop)
                uiView.centerVertical()
        }
        if (rootElement == null) {
            root?.removeAllChildren()
            root = null
        } else if (root == null) {
            root = ViewWithTreeElement(rootElement)
            setRootView(root!!.uiViewHolder)
            root?.updateChildren()
        } else {
            root?.updateView(rootElement, replaceOnParent = { _, newViewHolder ->
                subviews.forEach { it.removeFromSuperview() }
                setRootView(newViewHolder)
            })
        }

    }

}

class ViewWithTreeElement(
    treeElement: TreeElement
) {

    var treeElement = treeElement
        private set
    var uiViewHolder: UiViewHolder = MosaikViewCommon.buildUiViewHolder(treeElement)
        private set
    private val children: MutableList<ViewWithTreeElement> = LinkedList<ViewWithTreeElement>()

    fun updateView(
        newTreeElement: TreeElement,
        replaceOnParent: (oldView: UiViewHolder, newView: UiViewHolder) -> Unit,
    ) {
        if (newTreeElement.createdAtContentVersion > treeElement.createdAtContentVersion) {
            treeElement = newTreeElement

            // if the element changed, remove it from parent and readd the new one
            val newViewHolder = MosaikViewCommon.buildUiViewHolder(treeElement)
            replaceOnParent(uiViewHolder, newViewHolder)
            newViewHolder.onAddedToSuperview()

            uiViewHolder = newViewHolder
        }

        // resource bytes might have an update
        treeElement.getResourceBytes?.let {
            uiViewHolder.resourceBytesAvailable(it)
        }

        updateChildren()
    }

    fun updateChildren() {
        val viewGroupHolder = uiViewHolder as? ViewGroupHolder ?: return

        if (treeElement.children.map { it.createdAtContentVersion } !=
            children.map { it.treeElement.createdAtContentVersion }) {
            // something changed

            // remove all children
            removeAllChildren()

            // then add the new elements
            treeElement.children.forEach {
                val newElem = ViewWithTreeElement(it)
                viewGroupHolder.addSubView(newElem.uiViewHolder)
                newElem.uiViewHolder.onAddedToSuperview()
                children.add(newElem)
            }

            // TODO room for improvement: swap a single element when only one has changed
        }

        children.forEach {
            it.updateView(it.treeElement, viewGroupHolder::replaceSubView)
        }
    }

    fun removeAllChildren() {
        (uiViewHolder as? ViewGroupHolder)?.removeAllChildren()
        children.clear()
    }


}