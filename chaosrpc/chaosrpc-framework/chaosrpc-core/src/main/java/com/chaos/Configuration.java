package com.chaos;

import com.chaos.compress.Compressor;
import com.chaos.compress.impl.GZIPCompressor;
import com.chaos.discovery.RegistryConfig;
import com.chaos.loadbalance.LoadBalancer;
import com.chaos.loadbalance.impl.RoundRobinLoadBalancer;
import com.chaos.serialize.Serializer;
import com.chaos.serialize.impl.JdkSerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * 全局的配置类，代码配置 ----> xml配置 ----> 默认项
 * @author Chaos Wong
 */
@Data
@Slf4j
public class Configuration {

    // 配置信息 ----> 端口号
    private int port = 8088;

    // 配置信息 ----> 应用程序名字
    private String applicationName = "default";

    // 配置信息 ----> 注册中心
    private RegistryConfig registryConfig = new RegistryConfig("zookeeper://127.0.0.1:2181");

    // 配置信息 ----> 序列化协议
    private ProtocolConfig protocolConfig = new ProtocolConfig("jdk");

    // 配置信息 ----> 序列化使用的协议
    private String serializeType = "jdk";
    private Serializer serializer = new JdkSerializer();

    // 配置信息 ----> 压缩使用的协议
    private String compressType = "gzip";
    private Compressor compressor = new GZIPCompressor();

    // 配置信息 ----> Id生成器
    private IdGenerator idGenerator = new IdGenerator(1, 2);

    // 配置信息 ----> 负载均衡策略
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();

    // 读xml，dom4j
    public Configuration() {
        // 读取xml获取上边的信息
        loadFromXml(this);
    }

    /**
     * 从配置文件中读取配置信息 我们不使用dom4j
     * @param configuration 配置实例
     */
    private void loadFromXml(Configuration configuration) {
        try {
            // 1.创建一个document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // 禁用dtd校验
            factory.setValidating(false);
            // 禁用外部实体解析
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("chaosrpc.xml");
            Document doc = builder.parse(inputStream);

            // 2.获取一个xpath解析器
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();

            // 3.解析所有的标签
            configuration.setPort(resolvePort(doc, xpath));
            configuration.setApplicationName(resolveApplicationName(doc, xpath));
            configuration.setIdGenerator(resolveIdGenerator(doc, xpath));
            configuration.setRegistryConfig(resolveRegistryConfig(doc, xpath));
            configuration.setCompressType(resolveCompressType(doc, xpath));
//            configuration.setCompressor(resolveCompressor(doc, xpath));
            configuration.setSerializeType(resolveSerializeType(doc, xpath));
            configuration.setProtocolConfig(new ProtocolConfig(this.serializeType));
//            configuration.setSerializer(resolveSerializer(doc, xpath));
            configuration.setLoadBalancer(resolveLoadBalancer(doc, xpath));
        }  catch (SAXException | ParserConfigurationException | IOException e) {
            log.info("未发现相关或解析配置文件的时候发生了异常，将选用默认配置", e);
        }
    }

    /**
     * 解析序列化的具体实现
     * @param doc 文档
     * @param xpath xpath解析器
     * @return 序列化的具体实现
     */
    private Serializer resolveSerializer(Document doc, XPath xpath) {
        String expression = "/configuration/serializer";
        return parseObject(doc, xpath, expression, null);
    }

    /**
     * 解析序列化的方式
     * @param doc 文档
     * @param xpath xpath解析器
     * @return 序列化的方式
     */
    private String resolveSerializeType(Document doc, XPath xpath) {
        String expression = "/configuration/serializeType";
        return parseString(doc, xpath, expression, "type");
    }

    /**
     * 解析压缩的方式
     * @param doc 文档
     * @param xpath xpath解析器
     * @return 压缩的方式
     */
    private String resolveCompressType(Document doc, XPath xpath) {
        String expression = "/configuration/compressType";
        return parseString(doc, xpath, expression, "type");
    }

    /**
     * 解析压缩的具体实现
     * @param doc 文档
     * @param xpath xpath解析器
     * @return 压缩的具体实现
     */
    private Compressor resolveCompressor(Document doc, XPath xpath) {
        String expression = "/configuration/compressor";
        return parseObject(doc, xpath, expression, null);
    }

    /**
     * 解析负载均衡
     * @param doc 文档
     * @param xpath xpath解析器
     * @return 负载均衡策略
     */
    private LoadBalancer resolveLoadBalancer(Document doc, XPath xpath) {
        String expression = "/configuration/loadBalancer";
        return parseObject(doc, xpath, expression, null);
    }

    /**
     * 解析注册中心
     * @param doc 文档
     * @param xpath xpath解析器
     * @return url
     */
    private RegistryConfig resolveRegistryConfig(Document doc, XPath xpath) {
        String expression = "/configuration/registry";
        String url = parseString(doc, xpath, expression, "url");
        return new RegistryConfig(url);
    }

    /**
     * 解析idGenerator
     * @param doc 文档
     * @param xpath xpath解析器
     * @return id生成器
     */
    private IdGenerator resolveIdGenerator(Document doc, XPath xpath) {
        String expression = "/configuration/idGenerator";
        String aClass = parseString(doc, xpath, expression, "class");
        String dataCenterId = parseString(doc, xpath, expression, "dataCenterId");
        String machineId = parseString(doc, xpath, expression, "machineId");
        try{
            Class<?> clazz = Class.forName(aClass);
            Object instance = clazz.getConstructor(new Class[]{long.class, long.class})
                    .newInstance(Long.parseLong(dataCenterId), Long.parseLong(machineId));
            return (IdGenerator) instance;
        } catch (ClassNotFoundException | InvocationTargetException
                 | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析applicationName
     * @param doc 文档
     * @param xpath xpath解析器
     * @return applicationName
     */
    private String resolveApplicationName(Document doc, XPath xpath) {
        String expression = "/configuration/applicationName";
        return parseString(doc, xpath, expression);
    }

    /**
     * 解析端口号
     * @param doc 文档
     * @param xpath xpath解析器
     * @return 端口号
     */
    private int resolvePort(Document doc, XPath xpath) {
        String expression = "/configuration/port";
       return Integer.parseInt(Objects.requireNonNull(parseString(doc, xpath, expression)));
    }

    /**
     * 获取一个节点属性的值   <port num="8088"></>
     * @param doc 文档对象
     * @param xpath xpath解析器
     * @param expression xpath表达式
     * @param attributeName 节点名称
     * @return 节点的值
     */
    private String parseString(Document doc, XPath xpath, String expression, String attributeName){
        try {
            XPathExpression expr = xpath.compile(expression);
            // 我们的表达式帮我们获取节点
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return targetNode.getAttributes().getNamedItem(attributeName).getNodeValue();
        } catch (XPathExpressionException e) {
            log.error("解析表达式时发生了异常.", e);
        }
        return null;
    }

    /**
     * @param doc 文档对象
     * @param xpath xpath解析器
     * @param expression xpath表达式
     * @return 节点的值
     */
    private String parseString(Document doc, XPath xpath, String expression){
        try {
            XPathExpression expr = xpath.compile(expression);
            // 我们的表达式帮我们获取节点
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return targetNode.getTextContent();
        } catch (XPathExpressionException e) {
            log.error("解析表达式时发生了异常.", e);
        }
        return null;
    }

    /**
     * 解析一个节点，返回一个实例
     * @param doc 文档对象
     * @param xpath xpath解析器
     * @param expression xpath表达式
     * @param paramType 参数列表
     * @param params 参数
     * @return 配置的实例
     * @param <T> 泛型
     */
    private  <T> T parseObject(Document doc, XPath xpath, String expression, Class<?>[] paramType, Object... params){
        try {
            XPathExpression expr = xpath.compile(expression);
            // 我们的表达式帮我们获取节点
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            String className = targetNode.getAttributes().getNamedItem("class").getNodeValue();
            Class<?> aClass = Class.forName(className);
            Object instant = null;
            if(paramType == null) {
                instant = aClass.getConstructor().newInstance();
            } else {
                instant = aClass.getConstructor(paramType).newInstance(params);
            }
            return (T) instant;
        } catch (XPathExpressionException | ClassNotFoundException | InvocationTargetException |
                 InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            log.error("解析表达式时发生了异常.", e);
        }
        return null;
    }

}
