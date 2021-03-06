/*
 * Copyright 2006 Wyona
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.wyona.org/licenses/APACHE-LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wyona.yanel.core.navigation;

/**
 * Also see org.w3c.dom.Node
 */
public interface Node {

    public static int RESOURCE = 1;
    public static int COLLECTION = 2;

    /**
     * @return new child
     */
    public Node insertAsFirstChild(Node child);

    /**
     * @return new child
     */
    public Node insertBeforeChild(Node newChild, Node refChild);

    /**
     * @return new child
     */
    public Node insertAfterChild(Node newChild, Node refChild);

    /**
     * @return new child
     * @param type Node type (collection, resource)
     */
    public Node appendChild(Node child, int type);

    /**
     */
    public void removeChild(Node child);

    /**
     * Check if node is a resource
     */
    public boolean isResource();

    /**
     * Check if node is a collection
     */
    public boolean isCollection();

    /**
     * Get all child nodes of this node
     */
    public Node[] getChildren();

    /**
     *
     */
    public Node getParent();

    /**
     *
     */
    public Node getNextSibling();

    /**
     *
     */
    public Node getPreviousSibling();

    /**
     *
     */
    public String getPath();

    /**
     *
     */
    public String getName();

    /**
     *
     */
    public String getLabel();

    /**
     *
     */
    public void setLabel(String label);
}
