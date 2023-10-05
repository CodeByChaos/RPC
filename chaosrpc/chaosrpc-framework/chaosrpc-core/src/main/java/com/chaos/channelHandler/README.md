**1、服务调用方**

发送报文 writeAndFlush(Object) 请求

此Object应该是什么？应该包含一些什么样的信息？

ChaosrpcRequest
(
a.请求id(long)
b.压缩类型(byte)
c.序列化的方式
d.消息类型（普通请求、心跳检测请求）
f.负载 playload (接口的名字，方法的名字，参数列表，返回值类型)
)

pipeline就生效了，报文开始出站

----> 第一个处理器 in/out log

----> 第二个处理器 编码器（out）(转换 Object --> msg(请求报文)，序列化，压缩)

----> 第三个处理器 

**2、服务提供方**

通过netty接收请求报文

pipeline就生效了，报文开始入站

----> 第一个处理器 in/out log

----> 第二个处理器 解码器（in）(解压，反序列化，msg->chaosrpcrequest)

----> 想办法处理请求(in) chaosrpcrequest执行方法调用，处理请求。

**3、执行方法调用，返回结果**

**4、服务提供方**

发送报文 writeAndFlush(Object) 响应

pipeline就生效了，报文开始出站

----> 第一个处理器（out）(转换 Object --> msg(响应报文))

----> 第二个处理器（out）(序列化)

----> 第三个处理器（out）(压缩)

**5、服务调用方**

通过netty接收响应报文

pipeline就生效了，报文开始入站

----> 第一个处理器（in）(解压)

----> 第二个处理器（in）(反序列化)

----> 第三个处理器（in）(解析报文)

**6、得到结果返回**