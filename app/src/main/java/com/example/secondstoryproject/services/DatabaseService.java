package com.example.secondstoryproject.services;

import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.services.impl.ChatServiceImpl;
import com.example.secondstoryproject.services.impl.DonationServiceImpl;
import com.example.secondstoryproject.services.impl.UserServiceImpl;

/**
 * Singleton implementation of {@link IDatabaseService}.
 * Acts as a central access point (Service Locator) for all database-related operations
 * in the application, including:
 * - Users
 * - Donations
 * - Chats
 * This class ensures that only one instance exists and provides access to
 * the different service layers.
 * <p>Usage example:</p>
 * <pre>{@code
 * IDatabaseService db = DatabaseService.getInstance();
 * db.getUserService().get(userId, callback);
 * }</pre>
 *
 * @see IDatabaseService
 */
public class DatabaseService implements IDatabaseService {

    /** The single shared instance (lazy initialization) */
    private static DatabaseService instance;

    /** Service handling user-related operations */
    private final IUserService userService;

    /** Service handling donation-related operations */
    private final IDonationService DonationService;

    /** Service handling chat-related operations */
    private final IChatService chatService;

    /**
     * Private constructor.
     * Initializes all service implementations.
     * Use {@link #getInstance()} to access the singleton instance.
     */
    private DatabaseService() {
        userService = new UserServiceImpl();
        DonationService = new DonationServiceImpl();
        chatService = new ChatServiceImpl();
    }



    /**
     * Returns the singleton instance of {@link IDatabaseService}.
     * This method is thread-safe.
     * @return the shared database service instance
     */
    public static synchronized IDatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    /**
     * Returns the user service.
     */
    @Override
    public IUserService getUserService() {
        return userService;
    }

    /**
     * Returns the donation service.
     */
    @Override
    public IDonationService getDonationService() {
        return DonationService;
    }

    /**
     * Returns the chat service.
     */
    @Override
    public IChatService getChatService() { return chatService; }

}