package com.mafazaa.ainaa.domain

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object DomainNamesManager {
    private val root = TrieNode()

    /**
     * Inserts a domain into the Trie in reverse order.
     * e.g., "example.com" is inserted as "moc.elpmaxe"
     */
    fun insert(domain: String) {
        val cleanedDomain = domain.trim().lowercase()
        if (cleanedDomain.isEmpty()) return

        var current = root
        // Loop backwards through the string
        for (i in cleanedDomain.length - 1 downTo 0) {
            val char = cleanedDomain[i]
            current = current.children.getOrPut(char) { TrieNode() }
        }
        current.isEndOfDomain = true
    }

    /**
     * Checks if a domain or any of its parent domains exist in the Trie.
     * e.g., If "badsite.com" is blocked, searching "sub.badsite.com" will return true.
     */
    fun isBlocked(domain: String): Boolean {
        val cleanedDomain = domain.trim().lowercase()
        if (cleanedDomain.isEmpty()) return false

        var current = root
        // Search backwards
        for (i in cleanedDomain.length - 1 downTo 0) {
            val char = cleanedDomain[i]
            current = current.children[char] ?: return false

            // Optimization for VPN filtering:
            // If we match a base domain (e.g., "badsite.com"),
            // and the next character in the query is a dot '.',
            // then the sub-domain is automatically blocked.
            if (current.isEndOfDomain) {
                if (i == 0 || cleanedDomain[i - 1] == '.') {
                    return true
                }
            }
        }

        return current.isEndOfDomain
    }

    /**
     * Loads domains from an asset file into the provided Trie asynchronously.
     * @param context Android context to access assets
     * @param fileName The name of the file in the assets folder (e.g., "blocked_domains.txt")
     * @param trie The DomainTrie instance to populate
     */
    suspend fun loadAssetToTrie(context: Context, fileName: String) {
        withContext(Dispatchers.IO) {
            try {
                context.assets.open(fileName).use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String? = reader.readLine()
                        while (line != null) {
                            insert(line)
                            line = reader.readLine()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
fun String.getHost():String? {
        var host = trim()

        if (host.startsWith("https://")) {
            host = host.substring(8)
        } else if (host.startsWith("http://")) {
            host = host.substring(7)
        }

        // Remove path/query if present
        val slashIndex = host.indexOf('/')
        if (slashIndex != -1) {
            host = host.substring(0, slashIndex)
        }

        if (host.isEmpty()) return null
        if (host.startsWith('.') || host.endsWith('.')) return null
    return host

    }