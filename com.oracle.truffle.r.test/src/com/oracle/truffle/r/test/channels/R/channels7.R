# test remote vector transfer for read - should use the same vector

if (length(grep("FastR", R.Version()$version.string)) == 1) {
    ch <- .fastr.channel.create(1L)
    cx <- .fastr.context.create("SHARED_NOTHING")
    code <- "ch <- .fastr.channel.get(1L); x<-.fastr.channel.receive(ch); y<-x[1]; z<-.fastr.identity(x); .fastr.channel.send(ch, z)"
    .fastr.context.spawn(cx, code)
    y<-c(7, 42)
    z<-.fastr.identity(y)
    .fastr.channel.send(ch, y)
    x<-.fastr.channel.receive(ch)
    .fastr.context.join(cx)
    .fastr.channel.close(ch)
    print(x == z)
} else {
    print(TRUE)
}
