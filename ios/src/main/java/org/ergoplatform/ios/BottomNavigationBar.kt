package org.ergoplatform.ios

import org.ergoplatform.ios.ui.IMAGE_SETTINGS
import org.ergoplatform.ios.ui.IMAGE_WALLET
import org.ergoplatform.ios.ui.getAppDelegate
import org.ergoplatform.ios.wallet.WalletViewController
import org.robovm.apple.foundation.NSArray
import org.robovm.apple.uikit.*

class BottomNavigationBar : UITabBarController() {

    fun setupVcs() {
        val appDelegate = getAppDelegate()

        setViewControllers(
            NSArray(
                listOf(
                    createNavController(
                        WalletViewController(),
                        appDelegate.texts.get("title_wallets"), UIImage.systemImageNamed(IMAGE_WALLET)
                    ),
                    createNavController(
                        MyViewController(),
                        appDelegate.texts.get("title_settings"), UIImage.systemImageNamed(IMAGE_SETTINGS)
                    )
                )
            )
        )
    }

    private fun createNavController(
        rootViewController: UIViewController,
        title: String, image: UIImage
    ): UINavigationController {
        val navController = UINavigationController(rootViewController)
        navController.tabBarItem.title = title
        navController.tabBarItem.image = image
        //navController.navigationBar.setPrefersLargeTitles(true)
        rootViewController.navigationItem.title = title
        return navController
    }

    override fun viewDidLoad() {
        tabBar.barTintColor = UIColor.systemBackground()
        tabBar.tintColor = UIColor.label()
        setupVcs()
    }
}