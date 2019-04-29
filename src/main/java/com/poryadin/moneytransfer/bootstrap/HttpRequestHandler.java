package com.poryadin.moneytransfer.bootstrap;

import com.poryadin.moneytransfer.model.Result;
import com.poryadin.moneytransfer.handlers.RequestHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import com.poryadin.moneytransfer.routing.Route;
import com.poryadin.moneytransfer.routing.Router;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final CharSequence TYPE_JSON = new AsciiString("application/json; charset=UTF-8");
    private static final Charset ENCODING = StandardCharsets.UTF_8;

    private final Router router;

    public HttpRequestHandler(Router router) {
        this.router = router;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, FullHttpRequest msg) {
        if (HttpUtil.is100ContinueExpected(msg)) {
            context.writeAndFlush(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
        }
        QueryStringDecoder decoder = new QueryStringDecoder(msg.uri());

        RequestHandler handler = router.getHandler(new Route(msg.method(), decoder.path()));
        if (handler != null) {
            handle(context, msg, decoder, handler);
        } else {
            respond404(context);
        }
    }

    private void respond404(ChannelHandlerContext context) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.NOT_FOUND, Unpooled.EMPTY_BUFFER, false);
        context.write(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void handle(ChannelHandlerContext context, FullHttpRequest msg, QueryStringDecoder decoder, RequestHandler handler) {
        ByteBuf content = msg.content();
        String body = null;
        if (content.isReadable()) {
            body = content.toString(ENCODING);
        }

        try {
            Result result = handler.handle(decoder.parameters(), body);
            byte[] jsonResponse = result.toJson().getBytes(ENCODING);
            writeResponse(context, msg, Unpooled.wrappedBuffer(jsonResponse), TYPE_JSON,
                    String.valueOf(jsonResponse.length));

        } catch (Exception e) {
            //TODO log
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.wrappedBuffer(e.getMessage().getBytes(ENCODING)), false);
            context.write(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void writeResponse(ChannelHandlerContext ctx, HttpRequest request, ByteBuf buf, CharSequence contentType, CharSequence contentLength) {
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        FullHttpResponse response = buildResponse(buf, contentType, contentLength);

        if (keepAlive) {
            ctx.write(response, ctx.voidPromise());
        } else {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private FullHttpResponse buildResponse(ByteBuf buf, CharSequence contentType, CharSequence contentLength) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK, buf, false);
        HttpHeaders headers = response.headers();
        headers.set(HttpHeaderNames.CONTENT_TYPE, contentType);
        headers.set(HttpHeaderNames.CONTENT_LENGTH, contentLength);
        //TODO think of Expires and Cache-Control
        return response;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
}