package com.segmentation;
import java.io.UnsupportedEncodingException;

import com.sun.jna.Library;
import com.sun.jna.Native;


public class Test {
	// 定义接口CLibrary，继承自com.sun.jna.Library
		public interface CLibrary extends Library {
			// 定义并初始化接口的静态变量 这一个语句是来加载 dll 的， 注意 dll 文件的路径
			//可以是绝对路径也可以是相对路径，只需要填写 dll 的文件名，不能加后缀
			 CLibrary Instance = (CLibrary)Native.loadLibrary(
		        		System.getProperty("user.dir")+"\\source\\NLPIR", CLibrary.class);
			// 初始化函数声明
			public int NLPIR_Init(String sDataPath, int encoding,
					String sLicenceCode);
			//执行分词函数声明		
			public String NLPIR_ParagraphProcess(String sSrc, int bPOSTagged);
			//提取关键词函数声明
			public String NLPIR_GetKeyWords(String sLine, int nMaxKeyLimit,
					boolean bWeightOut);
			public String NLPIR_GetFileKeyWords(String sLine, int nMaxKeyLimit,
					boolean bWeightOut);
			//添加用户词典声明
			public int NLPIR_AddUserWord(String sWord);//add by qp 2008.11.10
			//删除用户词典声明
			public int NLPIR_DelUsrWord(String sWord);//add by qp 2008.11.10
			
			public String NLPIR_GetLastErrorMsg();
			//退出函数声明
			public void NLPIR_Exit();
			//文件分词声明
			public void NLPIR_FileProcess(String utf8File, String utf8FileResult, int i);
		}

		public static String transString(String aidString, String ori_encoding,
				String new_encoding) {
			try {
				return new String(aidString.getBytes(ori_encoding), new_encoding);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return null;
		}

		public static void main(String[] args) throws Exception {
			/*String argu = "";
			//String system_charset = "GBK";//GBK----0
			String system_charset = "UTF-8";
			int charset_type = 1;
			//文件分词的输入和输出
			String utf8File = "test/18届三中全会.TXT";
			String utf8FileResult = "test/18届三中全会-result.TXT";

			
			int init_flag = CLibrary.Instance.NLPIR_Init("", 1, "0");;
			String nativeBytes = null;

			if (0 == init_flag) {
				nativeBytes = CLibrary.Instance.NLPIR_GetLastErrorMsg();
				System.err.println("初始化失败！fail reason is "+nativeBytes);
				return;
			}

			String sInput = "去年开始，打开百度李毅吧，满屏的帖子大多含有“屌丝”二字，一般网友不仅不懂这词什么意思，更难理解这个词为什么会这么火。然而从下半年开始，“屌丝”已经覆盖网络各个角落，人人争说屌丝，人人争当屌丝。" + 
							"从遭遇恶搞到群体自嘲，“屌丝”名号横空出世";

			//String nativeBytes = null;
			try {
				//参数0表示不带词性，参数1表示带有词性
				nativeBytes = CLibrary.Instance.NLPIR_ParagraphProcess(sInput, 0);

				System.out.println("分词结果为： " + nativeBytes);

				//增加用户词典后
				CLibrary.Instance.NLPIR_AddUserWord("满屏的帖子 n");
				CLibrary.Instance.NLPIR_AddUserWord("更难理解 n");
				nativeBytes = CLibrary.Instance.NLPIR_ParagraphProcess(sInput, 1);
				System.out.println("增加用户词典后分词结果为： " + nativeBytes);
				//删除用户词典后
				CLibrary.Instance.NLPIR_DelUsrWord("更难理解");
				nativeBytes = CLibrary.Instance.NLPIR_ParagraphProcess(sInput, 1);
				System.out.println("删除用户词典后分词结果为： " + nativeBytes);
				//
//				CLibrary.Instance.NLPIR_FileProcess(utf8File, utf8FileResult,0);
				CLibrary.Instance.NLPIR_FileProcess(utf8File, utf8FileResult,1);
						
						
				int nCountKey = 0;
				String nativeByte = CLibrary.Instance.NLPIR_GetKeyWords(sInput, 10,false);

				System.out.print("关键词提取结果是：" + nativeByte);

				nativeByte = CLibrary.Instance.NLPIR_GetFileKeyWords("test\\屌丝，一个字头的诞生.TXT.txt", 10,false);

				System.out.print("关键词提取结果是：" + nativeByte);

				

				CLibrary.Instance.NLPIR_Exit();

			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}*/

		}

}
