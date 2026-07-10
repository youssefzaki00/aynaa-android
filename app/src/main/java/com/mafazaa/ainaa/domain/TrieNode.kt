package com.mafazaa.ainaa.domain

class TrieNode {
    val children = HashMap<Char, TrieNode>()
    var isEndOfDomain = false
}