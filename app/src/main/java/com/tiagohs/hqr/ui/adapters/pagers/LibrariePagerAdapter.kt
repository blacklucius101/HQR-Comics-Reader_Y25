package com.tiagohs.hqr.ui.adapters.pagers

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.tiagohs.hqr.ui.views.fragments.DownloadFragment
import com.tiagohs.hqr.ui.views.fragments.FavoritesFragment

class LibrariePagerAdapter(
       fm: FragmentManager,
       var tabsName: List<String>) : FragmentPagerAdapter(fm) {

    private val TAB_FAVORITE = 0
    private val TAB_DOWNLOAD = 1

    override fun getCount(): Int  = tabsName.size

    override fun getItem(tabSelect: Int): Fragment {

        when (tabSelect) {
            TAB_FAVORITE -> return FavoritesFragment.newFragment()
            TAB_DOWNLOAD -> return DownloadFragment.newFragment()
            else -> return FavoritesFragment.newFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return tabsName.get(position)
    }
}