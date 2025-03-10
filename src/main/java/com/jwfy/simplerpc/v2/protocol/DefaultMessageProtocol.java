package com.jwfy.simplerpc.v2.protocol;


import com.jwfy.simplerpc.v2.core.RpcRequest;
import com.jwfy.simplerpc.v2.core.RpcResponse;
import com.jwfy.simplerpc.v2.serialize.JavaInnerSerialize;
import com.jwfy.simplerpc.v2.serialize.SerializeProtocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * 套接字的io流和服务端、客户端的数据传输
 *
 * @author jwfy
 */
public class DefaultMessageProtocol implements MessageProtocol {

    private SerializeProtocol serializeProtocol;

    public DefaultMessageProtocol() {
        // this.serializeProtocol = new HessianSerialize();
        this.serializeProtocol = new JavaInnerSerialize();
    }

    public void setSerializeProtocol(SerializeProtocol serializeProtocol) {
        // 可替换序列化协议
        this.serializeProtocol = serializeProtocol;
    }

    @Override
    public RpcRequest serviceToRequest(InputStream inputStream) {
        try {
            // 2、bytes -> request 反序列化
            byte[] bytes = readBytes(inputStream);
            // System.out.println("[2]服务端反序列化出obj:[" + new String(bytes) + "], length:" + bytes.length);
            System.out.println("[2]服务端反序列化出obj length:" + bytes.length);
            RpcRequest request = serializeProtocol.deserialize(RpcRequest.class, bytes);
            return request;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> void serviceGetResponse(RpcResponse<T> response, OutputStream outputStream) {
        try {
            // 3、把response 序列化成bytes 传给客户端
            byte[] bytes = serializeProtocol.serialize(RpcResponse.class, response);
            // System.out.println("[3]服务端序列化出bytes:[" + new String(bytes) + "], length:" + bytes.length);
            System.out.println("[3]服务端序列化出bytes length:" + bytes.length);
            outputStream.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clientToRequest(RpcRequest request, OutputStream outputStream) {
        try {
            // 1、先把这个request -> bytes 序列化掉
            byte[] bytes = serializeProtocol.serialize(RpcRequest.class, request);
            // System.out.println("[1]客户端序列化出bytes:[" + new String(bytes) + "], length:" + bytes.length);
            System.out.println("[1]客户端序列化出bytes length:" + bytes.length);
            outputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public <T> RpcResponse<T>  clientGetResponse(InputStream inputStream) {
        try {
            // 4、bytes 反序列化成response
            byte[] bytes = readBytes(inputStream);

            // System.out.println("[4]客户端反序列化出bytes:[" + new String(bytes) + "], length:" + bytes.length);
            System.out.println("[4]客户端反序列化出bytes length:" + bytes.length);
            RpcResponse response = serializeProtocol.deserialize(RpcResponse.class, bytes);

            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] readBytes(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new RuntimeException("input为空");
        }
        return fun2(inputStream);
    }

    private byte[] fun1(InputStream inputStream) throws IOException {
        // 有个前提是数据最大是1024，并没有迭代读取数据
        byte[] bytes = new byte[1024];
        int count = inputStream.read(bytes, 0, 1024);
        return Arrays.copyOf(bytes, count);
    }

    private byte[] fun2(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int bufesize = 1024;
        while (true) {
            byte[] data = new byte[bufesize];
            int count = inputStream.read(data,0,bufesize);
            byteArrayOutputStream.write(data, 0, count);
            if (count < bufesize) {
                break;
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 有问题的fun3，调用之后会阻塞在read，可通过jstack查看相关信息
     * @param inputStream
     * @return
     * @throws IOException
     */
    private byte[] fun3(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int bufesize = 1024;

        byte[] buff = new byte[bufesize];
        int rc = 0;
        while ((rc = inputStream.read(buff, 0, bufesize)) > 0) {
            byteArrayOutputStream.write(buff, 0, rc);
            buff = new byte[bufesize];
        }
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return bytes;
    }

}
