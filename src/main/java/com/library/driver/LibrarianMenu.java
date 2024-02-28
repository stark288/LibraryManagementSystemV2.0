/**
 * This is the class for Librarian Menu
 * @author SivaSankar
 */
package com.library.driver;
import com.library.accounts.AccountManagement;
import com.library.Layout;
import com.library.exceptions.*;
import com.library.users.Admin;
import java.sql.SQLException;
import java.util.Scanner;
import com.library.search.Catalogue;
import com.library.services.Reservation;

/**
 * This class is used to perform the operations of the Librarian Menu
 */
public class LibrarianMenu  {
        Scanner sc = new Scanner(System.in);
        AccountManagement accountManagement = new AccountManagement();
        Admin admin = new Admin();
        Catalogue catalogue = new Catalogue();
        Reservation reservation = new Reservation();
        static Driver driver = new Driver();


        public void PatronMenu() throws SQLException, SqlConnectionException, InvalidDateFormatException, ValidPasswordExceptions, ValidemailExceptions{
            Layout.PatronMenu();
            System.out.println("Enter your choice");
            int choice = driver.getValidInput();
            sc.nextLine();
            switch (choice) {
                case 1:
                	try {
                    accountManagement.pauseMembership();
                    PatronMenu();
                    break;
				} catch (Exception e) {
					System.out.println("Invalid input");
					PatronMenu();
				}
                case 2:
                	try {
                    accountManagement.resumeMembership();
                    PatronMenu();
                    break;
                	} catch (Exception e) {
                		       System.out.println("Invalid input");
                	}
                case 3:
                    Librarian();
                    break;

                default:
                    System.out.println("Invalid choice for Patron Menu");
                    PatronMenu();
            }
        }

		/**
		 * This method is used to perform the operations of the Book Menu
		 * 
		 */
        public void BookMenu() throws SQLException, SqlConnectionException, InvalidDateFormatException, ValidPasswordExceptions, ValidemailExceptions{
            Layout.BookMenu();
            System.out.println("Enter your choice");
            int choice = driver.getValidInput();
            sc.nextLine();
            switch (choice) {
            
                case 1:
                	try {
                    Admin.addBook();
                    BookMenu();
                    break;
				} catch (Exception e) {
					System.out.println("Invalid input");
					BookMenu();
				}
                case 2:
                	try {
                    Admin.updateBook();
                    BookMenu();
                    break;
				} catch (Exception e) {
					System.out.println("Invalid input");
				}
                case 3:
                	try {
                    Admin.viewBooks();
                    BookMenu();
                    break;
				} catch (Exception e) {
					System.out.println("Invalid input");
				}
                case 4:
                	try {
                    Catalogue.search();
                    BookMenu();
                    break;
				} catch (Exception e) {
					System.out.println("Invalid input");
				}
                case 5:
                	try {
                    Admin.removeBook();
                    BookMenu();
                    break;
				} catch (Exception e) {
					System.out.println("Invalid input");
				}
                case 6:
                	try {
                    reservation.displayRequestedReservations();
                    BookMenu();
                    break;
				} catch (Exception e) {
					System.out.println("Invalid input");
				}
                case 9:
                    Librarian();
                    break;
                case 7:
                	try {
                    reservation.approveReservation();
                    BookMenu();
                    break;
				} catch (Exception e) {
					System.out.println("Invalid input");
				}
                case 8:
                	try {
                    reservation.rejectReservation();
                    BookMenu();
                    break;
				} catch (Exception e) {
					System.out.println("Invalid input");
				}
                default:
                    System.out.println("Invalid choice");
                    BookMenu();
            }
        }
        
		/**
		 * This method is used to perform the operations of the Librarian Menu
		 * 
		 */

        public void Librarian() throws SQLException, SqlConnectionException, InvalidDateFormatException, ValidPasswordExceptions, ValidemailExceptions{
            Layout.LibrarianMenu();
            System.out.println("Enter your choice");
            int choice = driver.getValidInput();
            sc.nextLine();
            switch (choice) {

                case 1:
                	try {
                    PatronMenu();
                    break;
				} catch (Exception e) {
					System.out.println("Invalid input");
				}
                case 2:
                	try {
                    BookMenu();
                    break;
				} catch (Exception e) {
					System.out.println("Invalid input");
				}
                case 3:
                	try {
                    Driver driver = new Driver();
                    driver.mainmenu();
                    break;
				} catch (Exception e) {
					System.out.println("Invalid input");
				}
                default:
                    System.out.println("Invalid choice");
                    Librarian();
            }
        }
}

