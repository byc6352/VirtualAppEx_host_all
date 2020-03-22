/**
 *
 */
package encrypt;
/**
 * @author Administrator
 *
 */
public class Base64 {

	//Constructor
	public Base64() {

	}

	//private static final String base64Code= "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	private static final String     CODES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

	public static String encode(String srcStr) {
		//有效值检查
		if(srcStr == null || srcStr.length() == 0) {
			return srcStr;
		}
		//将明文的ASCII码转为二进制位字串
		char[] srcStrCh= srcStr.toCharArray();
		StringBuilder asciiBinStrB= new StringBuilder();
		String asciiBin= null;
		for(int i= 0; i< srcStrCh.length; i++) {
			asciiBin= Integer.toBinaryString((int)srcStrCh[i]);
			while(asciiBin.length()< 8) {
				asciiBin= "0"+ asciiBin;
			}
			asciiBinStrB.append(asciiBin);
		}
		//跟据明文长度在二进制位字串尾部补“0”
		while(asciiBinStrB.length()% 6!= 0) {
			asciiBinStrB.append("0");
		}
		String asciiBinStr= String.valueOf(asciiBinStrB);
		//将上面得到的二进制位字串转为Value，再跟据Base64编码表将之转为Encoding
		char[] codeCh= new char[asciiBinStr.length()/ 6];
		int index= 0;
		for(int i= 0; i< codeCh.length; i++) {
			index= Integer.parseInt(asciiBinStr.substring(0, 6), 2);
			asciiBinStr= asciiBinStr.substring(6);
			codeCh[i]= CODES.charAt(index);
		}
		StringBuilder code= new StringBuilder(String.valueOf(codeCh));
		//跟据需要在尾部添加“=”
		if(srcStr.length()% 3 == 1) {
			code.append("==");
		} else if(srcStr.length()% 3 == 2) {
			code.append("=");
		}
		//每76个字符加一个回车换行符（CRLF）
		int i= 76;
		while(i< code.length()) {
			code.insert(i, "\r\n");
			i+= 76;
		}
		code.append("\r\n");
		return String.valueOf(code);
	}
	// base64加密
	public static String Encode(byte[] in) {
		StringBuilder out = new StringBuilder((in.length * 4) / 3);
		int b;
		for (int i = 0; i < in.length; i += 3) {
			b = (in[i] & 0xFC) >> 2;
			out.append(CODES.charAt(b));
			b = (in[i] & 0x03) << 4;
			if (i + 1 < in.length) {
				b |= (in[i + 1] & 0xF0) >> 4;
				out.append(CODES.charAt(b));
				b = (in[i + 1] & 0x0F) << 2;
				if (i + 2 < in.length) {
					b |= (in[i + 2] & 0xC0) >> 6;
					out.append(CODES.charAt(b));
					b = in[i + 2] & 0x3F;
					out.append(CODES.charAt(b));
				} else {
					out.append(CODES.charAt(b));
					out.append('=');
				}
			} else {
				out.append(CODES.charAt(b));
				out.append("==");
			}
		}
		return out.toString();
	}
	// base64解密
	public static byte[] Decode(String input) {
		if (input.length() % 4 != 0) {
			throw new IllegalArgumentException("Invalid base64 input");
		}
		byte decoded[] = new byte[((input.length() * 3) / 4) - (input.indexOf('=') > 0 ? (input.length() - input.indexOf('=')) : 0)];
		char[] inChars = input.toCharArray();
		int j = 0;
		int b[] = new int[4];
		for (int i = 0; i < inChars.length; i += 4) {
			// This could be made faster (but more complicated) by precomputing
			// these index locations.
			b[0] = CODES.indexOf(inChars[i]);
			b[1] = CODES.indexOf(inChars[i + 1]);
			b[2] = CODES.indexOf(inChars[i + 2]);
			b[3] = CODES.indexOf(inChars[i + 3]);
			decoded[j++] = (byte) ((b[0] << 2) | (b[1] >> 4));
			if (b[2] < 64) {
				decoded[j++] = (byte) ((b[1] << 4) | (b[2] >> 2));
				if (b[3] < 64) {
					decoded[j++] = (byte) ((b[2] << 6) | b[3]);
				}
			}
		}
		return decoded;
	}
	public static String decode(String srcStr) {
		//有效值检查
		if(srcStr == null || srcStr.length() == 0) {
			return srcStr;
		}
		//检测密文中“=”的个数后将之删除，同时删除换行符
		int eqCounter= 0;
		if(srcStr.endsWith("==")) {
			eqCounter= 2;
		} else if(srcStr.endsWith("=")) {
			eqCounter= 1;
		}
		srcStr= srcStr.replaceAll("=", "");
		srcStr= srcStr.replaceAll("\r\n", "");
		//跟据Base64编码表将密文（Encoding）转为对应Value，然后转为二进制位字串
		char[] srcStrCh= srcStr.toCharArray();
		StringBuilder indexBinStr= new StringBuilder();
		String indexBin= null;
		for(int i= 0; i< srcStrCh.length; i++) {
			indexBin= Integer.toBinaryString(CODES.indexOf((int)srcStrCh[i]));
			while(indexBin.length()< 6) {
				indexBin= "0"+ indexBin;
			}
			indexBinStr.append(indexBin);
		}
		//删除因编码而在尾部补位的“0”后得到明文的ASCII码的二进制位字串
		if(eqCounter == 1) {
			indexBinStr.delete(indexBinStr.length()- 2, indexBinStr.length());
		} else if(eqCounter == 2) {
			indexBinStr.delete(indexBinStr.length()- 4, indexBinStr.length());
		}
		String asciiBinStr= String.valueOf(indexBinStr);
		//将上面得到的二进制位字串分隔成字节后还原成明文
		String asciiBin= null;
		char[] ascii= new char[asciiBinStr.length()/ 8];
		for(int i= 0; i< ascii.length; i++) {
			asciiBin= asciiBinStr.substring(0, 8);
			asciiBinStr= asciiBinStr.substring(8);
			ascii[i]= (char)Integer.parseInt(asciiBin, 2);
		}
		return String.valueOf(ascii);
	}


}
