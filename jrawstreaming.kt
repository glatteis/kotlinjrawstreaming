import net.dean.jraw.RedditClient
import net.dean.jraw.models.Comment
import net.dean.jraw.models.Message
import net.dean.jraw.models.Submission
import net.dean.jraw.models.Thing
import net.dean.jraw.paginators.*

/**
 * PLEASE DO NOT REMOVE THIS CAPTION.
 * This program was made by glatteis. https://github.com/glatteis
 * I grant the ability for anyone to copy, modify and use this program, commercially and uncommercially.
 * This program may not be released on its own without the author's permission.
 */

object JRAWStreaming {

    /*
    Attention: What this bot does is no actual streaming. It just makes a lot of requests. You have to keep in
    mind that a reddit bot can only make a certain number of requests per minute. Set "interval" mindfully (it's in ms).

    Sample usage: print all comments from me_irl and meirl.
    BotUtils.streamComments("meirl+me_irl", client, 2000, {
        println(it.body)
    })
     */

    fun streamComments(subreddit: String, client: RedditClient, interval: Int, lambda: (Comment) -> Unit) {
        val commentStream = CommentStream(client, subreddit)
        commentStream.sorting = Sorting.NEW
        commentStream.setLimit(200)
        stream(interval, lambda, commentStream)
    }

    fun streamPosts(subreddit: String, client: RedditClient, interval: Int, lambda: (Submission) -> Unit) {
        val paginator = SubredditPaginator(client, subreddit)
        paginator.sorting = Sorting.NEW
        paginator.setLimit(200)
        stream(interval, lambda, paginator)
    }

    fun streamInbox(where: String, client: RedditClient, interval: Int, lambda: (Message) -> Unit) {
        val paginator = InboxPaginator(client, where)
        paginator.sorting = Sorting.NEW
        paginator.setLimit(200)
        stream(interval, lambda, paginator)
    }

    /*
    Well, you get the idea how you can stream different paginators. You can build your own streaming methods based on that.
     */

    fun <T : Thing>stream(interval: Int, lambda: (T) -> Unit, iterable: RedditIterable<T>) {
        var lastId = ""
        var firstId = ""
        while (true) {
            iterable.reset()
            val lastTime = System.currentTimeMillis()
            var first = true
            firstId = ""
            whileLoop@ while (iterable.hasNext()) {
                val submissions = iterable.next()
                for (s in submissions) {
                    if (first) {
                        first = false
                        firstId = s.id
                        if (lastId == "") break@whileLoop
                    }
                    if (lastId.equals(s.id)) {
                        break@whileLoop
                    }
                    lambda(s)
                }
            }
            if ((System.currentTimeMillis() - lastTime) < interval)
                Thread.sleep(interval - (System.currentTimeMillis() - lastTime))
            lastId = firstId
        }
    }

}

