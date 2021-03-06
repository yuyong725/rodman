/**
 * @author 余勇
 * @date 2021年03月06日 19:02:00
 */
public class CatException extends RuntimeException {

	public CatException() {
		super();
	}

	public CatException(String msg) {
		super(msg);
	}

	public CatException(String msg, Exception e) {
		super(msg, e);
	}
}
