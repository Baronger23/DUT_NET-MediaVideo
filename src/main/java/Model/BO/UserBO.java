package Model.BO;

import Model.Bean.User;
import Model.DAO.UserDAO;

public class UserBO {
	private UserDAO userDAO;

	public UserBO() {
		this.userDAO = new UserDAO();
	}

	public User xacThucDangNhap(String username, String password) {
		return userDAO.xacThucDangNhap(username, password);
	}
	
	public boolean dangKyUser(String username, String password, String email) {
		return userDAO.dangKyUser(username, password, email);
	}
	
	public boolean kiemTraUsernameTonTai(String username) {
		return userDAO.kiemTraUsernameTonTai(username);
	}
	
	public boolean kiemTraEmailTonTai(String email) {
		return userDAO.kiemTraEmailTonTai(email);
	}
}
