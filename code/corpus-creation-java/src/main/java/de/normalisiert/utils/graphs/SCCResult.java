/*
 * (BSD-2 license)
 *
 * Copyright (c) 2012, Frank Meyer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *     Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.normalisiert.utils.graphs;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class SCCResult
{
    private Set nodeIDsOfSCC = null;
    private Vector[] adjList = null;
    private int lowestNodeId = -1;

    public SCCResult(Vector[] adjList, int lowestNodeId)
    {
        this.adjList = adjList;
        this.lowestNodeId = lowestNodeId;
        this.nodeIDsOfSCC = new HashSet();
        if (this.adjList != null) {
            for (int i = this.lowestNodeId; i < this.adjList.length; i++) {
                if (this.adjList[i].size() > 0) {
                    this.nodeIDsOfSCC.add(new Integer(i));
                }
            }
        }
    }

    public Vector[] getAdjList()
    {
        return adjList;
    }

    public int getLowestNodeId()
    {
        return lowestNodeId;
    }
}
