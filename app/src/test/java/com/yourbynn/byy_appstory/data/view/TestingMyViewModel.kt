package com.yourbynn.byy_appstory.data.view

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.yourbynn.byy_appstory.DispatcherRule
import com.yourbynn.byy_appstory.data.Dummy
import com.yourbynn.byy_appstory.data.adapter.StoriesAdapter
import com.yourbynn.byy_appstory.data.getOrAwaitValue
import com.yourbynn.byy_appstory.data.pref.UserRepository
import com.yourbynn.byy_appstory.data.response.ListStoryItem
import com.yourbynn.byy_appstory.view.main.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class TestingMyViewModel {
        @get:Rule
        val instantExecutorRule = InstantTaskExecutorRule()

        @get:Rule
        val mainDispatcherRules = DispatcherRule()

        @Mock
        private lateinit var storyRepository: UserRepository

        @Test
        fun `when Get Story Should Not Null and Return Data`() = runTest {
            val dummyStory = Dummy.generateDummyStoryResponse()
            val data: PagingData<ListStoryItem> = StoryPagingSource.snapshot(dummyStory)
            val expectedStory = MutableLiveData<PagingData<ListStoryItem>>()
            expectedStory.value = data
            Mockito.`when`(storyRepository.getStories("token")).thenReturn(expectedStory)

            val mainViewModel = MainViewModel(storyRepository)
            val actualStory: PagingData<ListStoryItem> = mainViewModel.getStories("token").getOrAwaitValue()

            val differ = AsyncPagingDataDiffer(
                diffCallback = StoriesAdapter.DIFF_CALLBACK,
                updateCallback = noopListUpdateCallback,
                workerDispatcher = Dispatchers.Main,
            )
            differ.submitData(actualStory)

            Assert.assertNotNull(differ.snapshot())
            Assert.assertEquals(dummyStory.size, differ.snapshot().size)
            Assert.assertEquals(dummyStory[0], differ.snapshot()[0])
        }

        @Test
        fun `when Get Story Empty Should Return No Data`() = runTest {
            val data: PagingData<ListStoryItem> = PagingData.from(emptyList())
            val expectedStory = MutableLiveData<PagingData<ListStoryItem>>()
            expectedStory.value = data
            Mockito.`when`(storyRepository.getStories("token")).thenReturn(expectedStory)

            val mainViewModel = MainViewModel(storyRepository)
            val actualStory: PagingData<ListStoryItem> = mainViewModel.getStories("token").getOrAwaitValue()

            val differ = AsyncPagingDataDiffer(
                diffCallback = StoriesAdapter.DIFF_CALLBACK,
                updateCallback = noopListUpdateCallback,
                workerDispatcher = Dispatchers.Main,
            )
            differ.submitData(actualStory)

            Assert.assertEquals(0, differ.snapshot().size)
        }
    }

    class StoryPagingSource : PagingSource<Int, LiveData<List<ListStoryItem>>>() {
        companion object {
            fun snapshot(items: List<ListStoryItem>): PagingData<ListStoryItem> {
                return PagingData.from(items)
            }
        }

        override fun getRefreshKey(state: PagingState<Int, LiveData<List<ListStoryItem>>>): Int {
            return 0
        }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<ListStoryItem>>> {
            return LoadResult.Page(emptyList(), 0, 1)
        }
    }

    val noopListUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
}