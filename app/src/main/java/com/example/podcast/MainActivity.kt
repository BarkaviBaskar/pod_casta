package com.example.podcast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import coil.compose.rememberImagePainter
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : ComponentActivity() {
    @ExperimentalComposeUiApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "mainScreen") {
                composable("mainScreen") {
                    MainScreen(navController = navController)
                }
                composable(
                    "detailScreen/{itemId}",
                    arguments = listOf(navArgument("itemId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
                    DetailScreen(itemId = itemId, navController = navController)
                }
                composable("newListScreen") {
                    NewListScreen(navController = navController)
                }
            }
        }
    }
}
@Composable
fun NewListScreen(navController: NavController) {
    var podcastItems by remember { mutableStateOf<List<Item>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val httpClient = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    LaunchedEffect(key1 = Unit) {
        try {
            val response = httpClient.get<String>("https://feed.podbean.com/dailydoseofencouragement/feed.xml")
            podcastItems = parseXml(response)
        } catch (e: Exception) {
        } finally {
            isLoading = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(50.dp)
                    .wrapContentSize(Alignment.Center)
            )
        } else {
            PodcastList(podcastItems)
        }
    }
}
fun parseXml(xmlString: String): List<Item> {
    return try {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(InputSource(StringReader(xmlString)))
        val itemList = mutableListOf<Item>()

        val nodeList = document.getElementsByTagName("item")
        for (index in 0 until nodeList.length) {
            val element = nodeList.item(index) as Element
            val title = element.getElementsByTagName("title").item(0).textContent
            val link = element.getElementsByTagName("link").item(0).textContent
            val comment = element.getElementsByTagName("comments").item(0).textContent
            val pubDate = element.getElementsByTagName("pubDate").item(0).textContent
            val imageUrl = element.getElementsByTagName("itunes:image").item(0)?.attributes?.getNamedItem("href")?.textContent ?: ""

            itemList.add(Item(title, link, comment, pubDate, imageUrl))
        }

        itemList
    } catch (e: Exception) {
        emptyList()
    }
}

@Composable
fun PodcastList(podcastItems: List<Item>) {
    LazyColumn {
        items(podcastItems) { podcastItem ->
            PodcastItemCard(podcastItem)
        }
    }
}

@Composable
fun PodcastItemCard(podcastItem: Item) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Image(
                painter = rememberImagePainter(data = podcastItem.imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = podcastItem.title,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = podcastItem.publishDate,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = podcastItem.comment,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = podcastItem.link,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .padding(start=15.dp,top = 30.dp,end=15.dp,bottom=15.dp)

        ) {
            Text(
                "POD", fontSize = 25.sp,
                color = Color.Cyan,
                fontWeight = FontWeight.Bold
            )
            Text(
                "CASTS", fontSize = 50.sp,
                color = Color.Blue,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = {
                    navController.navigate("newListScreen")
                },
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text("Fetch Data and Navigate")
            }
            val gridItemsList =listOf( GridItem(
                1,
                "Live Morning Show",
                R.drawable.abi
            ),
                GridItem(
                    2,
                    "Reflections on the Saints",
                    R.drawable.abi
                ),
                GridItem(
                    3,
                    "Inspirational Thoughts Each Weekday",
                    R.drawable.abi
                ),
                GridItem(
                    4,
                    "A Saint for Each day of the year",
                    R.drawable.abi
                ),
                GridItem(
                    5,
                    "Explore Church History",
                    R.drawable.abi
                ),
                GridItem(
                    6,
                    "Splender of You",
                    R.drawable.abi
                ),
                GridItem(
                    7,
                    "Lord, Where are you Going?",
                    R.drawable.abi
                ))
            MyGrid(gridItemsList = gridItemsList,navController = navController)
        }
    }
}
@Composable
fun DetailScreen(itemId: Int, navController: NavController) {
    val selectedItem = getSelectedItem(itemId)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .height(200.dp)
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = selectedItem.imageResource),
                contentDescription = null,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(Color.Black, shape = CircleShape)
                    .padding(8.dp)
                    .align(Alignment.CenterStart)
                    .clickable {
                        navController.popBackStack()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_arrow_back_ios_24),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        Row(
            modifier = Modifier
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Blue, shape = CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_play_arrow_24),
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                Text(
                    text = "Summit, Confirmation",
                    fontSize = 20.sp,
                    color = Color.Blue,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Text(
                    text = "November 14,23",
                    fontSize = 16.sp,
                    color = Color.Blue,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            items(getMusicList()) { musicItem ->
                MusicListItem(musicItem = musicItem)
            }
        }
    }
}
fun getMusicList(): List<MusicItem> {
    return listOf(
        MusicItem(1,"Interview with Ashley Graham","November 13 ,2023" ),
        MusicItem(2,"Fostering Devotions","November 12 ,2023" ),
        MusicItem(3,"On the Road at SUMMIT","November 11 ,2023" ),
        MusicItem(4,"Month of All Saints","November 10 ,2023" ),
        MusicItem(5,"The Summit","November 9 ,2023" ),
        MusicItem(6,"Eucharistic Congress","November 8 ,2023" ),
        MusicItem(7,"Consequence of Sin","November 7 ,2023" ),
        MusicItem(8,"Roadmap Roundup","November 6 ,2023" ),
        MusicItem(9,"Redeemer of Man", "November 5 ,2023"),
        MusicItem(10,"What are you Afraid of?","November 4 ,2023" ),
        MusicItem(11,"Marian Apparitions","November 3 ,2023" )
    )
}

@Composable
fun MusicListItem(musicItem: MusicItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(Color.Blue, shape = CircleShape)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_play_arrow_24),
                contentDescription = null,
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = musicItem.title,
                fontSize = 16.sp,
                color = Color.Blue,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${musicItem.date}",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

fun getSelectedItem(itemId: Int): GridItem {
    return GridItem(1, "Live Morning Show", R.drawable.abi)
}

@Composable
fun MyGrid(gridItemsList: List<GridItem>, columns: Int = 2,navController: NavController) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(gridItemsList) { item ->
            GridItemCard(item = item, navController = navController)
        }
    }
}

@Composable
fun GridItemCard(item: GridItem,navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                navController.navigate("detailScreen/${item.id}")
            }
    ) {
        Image(
            painter = painterResource(id=item.imageResource),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = item.title,
            fontSize = 16.sp,
            color = Color.Blue,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)

        )
    }
}

data class GridItem(
    val id:Int,
    val title: String,
    val imageResource: Int
)
data class MusicItem(
    val id: Int,
    val title: String,
    val date: String
)

@Serializable
data class Item(
    @SerialName("title") val title: String,
    @SerialName("link") val link: String,
    @SerialName("comment") val comment: String,
    @SerialName("publishDate") val publishDate: String,
    @SerialName("imageUrl") val imageUrl: String
)



@Preview(showBackground = true)
@Composable
fun PreviewGridItemCard() {
    GridItemCard(GridItem(0," ", 0),rememberNavController())
}