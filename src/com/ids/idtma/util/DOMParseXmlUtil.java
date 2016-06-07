package com.ids.idtma.util;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ids.idtma.entity.UpgradeEntity;

public class DOMParseXmlUtil {

	/**
	 * 使用DOM获取XML文件的内容
	 * 
	 * @param inStream
	 * @return
	 * @throws Throwable
	 */
	public static UpgradeEntity getUpgradeEntity(InputStream inStream)
			throws Exception {
		UpgradeEntity entity = new UpgradeEntity();
		// DOM文件创建工厂
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// DOM创建对象
		DocumentBuilder builder = factory.newDocumentBuilder();
		// 获取XML的DOM
		Document document = builder.parse(inStream);
		// 获取XML文件的内容
		Element root = document.getDocumentElement();
		// 获取当前节点的子节点
		NodeList childNode = root.getChildNodes();
		for (int i = 0; i < childNode.getLength(); i++) {
			if (childNode.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) childNode.item(i);
				if ("apk-file".equals(childElement.getNodeName())) {
					entity.setApkFile(childElement.getFirstChild()
							.getNodeValue());
				} else if ("version-code".equals(childElement.getNodeName())) {
					entity.setVersionCode(new Integer(childElement
							.getFirstChild().getNodeValue()));
				} else if ("version-name".equals(childElement.getNodeName())) {
					entity.setVersionName(childElement.getFirstChild()
							.getNodeValue());
				} else if ("version-description".equals(childElement
						.getNodeName())) {
					entity.setVersionDescription(childElement.getFirstChild()
							.getNodeValue());
				}
			}
		}
		return entity;
	}

}