package com.mrazoooz.contestants

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

private data class Contestant(val id: Long, val name: String, val score: Int)
private data class Question(val field: String, val text: String, val answer: String)
private val bank = listOf(
 Question("علوم","ما الكوكب المعروف بالكوكب الأحمر؟","المريخ"),
 Question("علوم","ما الغاز الذي تمتصه النباتات؟","ثاني أكسيد الكربون"),
 Question("علوم","ما العضو الذي يضخ الدم؟","القلب"),
 Question("جغرافيا","ما عاصمة المملكة العربية السعودية؟","الرياض"),
 Question("جغرافيا","ما أكبر قارات العالم؟","آسيا"),
 Question("جغرافيا","ما المحيط الأكبر؟","المحيط الهادئ"),
 Question("تاريخ","في أي عام انتهت الحرب العالمية الثانية؟","1945"),
 Question("تاريخ","من أول الخلفاء الراشدين؟","أبو بكر الصديق"),
 Question("رياضة","كم لاعباً في فريق كرة القدم؟","11 لاعباً"),
 Question("رياضة","كم شوطاً في مباراة كرة القدم؟","شوطان"),
 Question("تقنية","ماذا تعني CPU؟","وحدة المعالجة المركزية"),
 Question("تقنية","من مطور نظام Android؟","Google"),
 Question("معلومات عامة","كم عدد ألوان قوس قزح؟","سبعة"),
 Question("معلومات عامة","ما أسرع حيوان بري؟","الفهد")
)
private val colors = listOf("أزرق" to Color(0xFF2563EB),"أخضر" to Color(0xFF16845B),"أحمر" to Color(0xFFD14343),"برتقالي" to Color(0xFFEA7B24),"تركوازي" to Color(0xFF0891A5))

class MainActivity: ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { App() } }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable private fun App() {
 val ctx=LocalContext.current
 val prefs=remember { ctx.getSharedPreferences("contestants_store",0) }
 val people=remember { mutableStateListOf<Contestant>().apply {
  runCatching { val a=JSONArray(prefs.getString("people","[]") ?: "[]"); for(i in 0 until a.length()){val o=a.getJSONObject(i);add(Contestant(o.getLong("id"),o.getString("name"),o.getInt("score")))} }
 } }
 var page by remember { mutableStateOf("الرئيسية") }
 var query by remember { mutableStateOf("") }
 var edit by remember { mutableStateOf<Contestant?>(null) }
 var editor by remember { mutableStateOf(false) }
 var scorePerson by remember { mutableStateOf<Contestant?>(null) }
 var limited by remember { mutableStateOf(prefs.getBoolean("limited",false)) }
 var maxScore by remember { mutableIntStateOf(prefs.getInt("maxScore",100)) }
 var theme by remember { mutableStateOf(prefs.getString("themeMode","system") ?: "system") }
 var accentName by remember { mutableStateOf(prefs.getString("accentName","أخضر") ?: "أخضر") }
 var design by remember { mutableIntStateOf(prefs.getInt("design",1)) }
 var drawer by remember { mutableStateOf(false) }
 var field by remember { mutableStateOf("الكل") }
 var count by remember { mutableIntStateOf(5) }
 var questions by remember { mutableStateOf(emptyList<Question>()) }
 var qIndex by remember { mutableIntStateOf(0) }
 var selected by remember { mutableStateOf<Long?>(null) }
 var reveal by remember { mutableStateOf(false) }
 var correct by remember { mutableIntStateOf(0) }
 var wrong by remember { mutableIntStateOf(0) }
 var finished by remember { mutableStateOf(false) }
 LaunchedEffect(people.toList()) { val a=JSONArray();people.forEach { a.put(JSONObject().put("id",it.id).put("name",it.name).put("score",it.score)) };prefs.edit().putString("people",a.toString()).apply() }
 LaunchedEffect(limited,maxScore,theme,accentName,design) { prefs.edit().putBoolean("limited",limited).putInt("maxScore",maxScore).putString("themeMode",theme).putString("accentName",accentName).putInt("design",design).apply() }
 val dark=when(theme){"dark"->true;"light"->false;else->isSystemInDarkTheme()}
 val accent=colors.firstOrNull{it.first==accentName}?.second ?: Color(0xFF16845B)
 val scheme=if(dark) darkColorScheme(primary=accent,background=Color(0xFF111820),surface=Color(0xFF1C2731)) else lightColorScheme(primary=accent,background=Color(0xFFF4F6F9))
 val sorted=people.filter{it.name.contains(query,true)}.sortedByDescending{it.score}
 CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
  MaterialTheme(colorScheme=scheme) {
   ModalNavigationDrawer(drawerState=rememberDrawerState(if(drawer) DrawerValue.Open else DrawerValue.Closed),drawerContent={
    ModalDrawerSheet {
     Text("كشف المتسابقين",Modifier.padding(20.dp),style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
     listOf("الرئيسية","المتسابقون","الأسئلة العشوائية","الإعدادات").forEach { label ->
      NavigationDrawerItem(label={Text(label)},selected=page==label,onClick={page=label;drawer=false},modifier=Modifier.padding(horizontal=10.dp))
     }
    }
   }) {
    Scaffold(containerColor=MaterialTheme.colorScheme.background,
     topBar={
      if(design==1) Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically) {
       IconButton(onClick={drawer=true}){Icon(Icons.Default.Menu,null)}
       Text("كشف المتسابقين — "+page,Modifier.weight(1f),style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
       IconButton(onClick={page="الإعدادات"}){Icon(Icons.Default.Settings,null)}
      }
     },
     bottomBar={
      if(design==2) NavigationBar {
       listOf("الرئيسية","المتسابقون","الأسئلة العشوائية","الإعدادات").forEachIndexed { i,label ->
        NavigationBarItem(selected=page==label,onClick={page=label},icon={Icon(listOf(Icons.Default.Home,Icons.Default.Groups,Icons.Default.Quiz,Icons.Default.Settings)[i],null)},label={Text(listOf("الرئيسية","المتسابقون","الأسئلة","الإعدادات")[i])})
       }
      }
     },
     floatingActionButton={if(page=="الرئيسية"||page=="المتسابقون") FloatingActionButton(onClick={edit=null;editor=true},containerColor=accent){Icon(Icons.Default.Add,null)}}
    ) { pad ->
     Column(Modifier.fillMaxSize().padding(pad).padding(horizontal=16.dp)) {
      if(design==2) {
       Text("لوحة المنافسة",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Black,modifier=Modifier.padding(top=16.dp))
       Text("تابع الترتيب وأدر جولات التحدي",color=MaterialTheme.colorScheme.onSurfaceVariant)
      }
      when(page) {
       "الرئيسية" -> {
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
         Stat("المتسابقون",people.size.toString(),Modifier.weight(1f))
         Stat("أعلى نتيجة",(people.maxOfOrNull{it.score}?:0).toString(),Modifier.weight(1f))
         Stat("مجموع النقاط",people.sumOf{it.score}.toString(),Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Text("أفضل المتسابقين",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
        LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)) { items(sorted.take(5),key={it.id}){ PersonRow(it,limited,maxScore,{scorePerson=it},{edit=it;editor=true},{people.removeAll{p->p.id==it.id}}) } }
       }
       "المتسابقون" -> {
        OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),placeholder={Text("ابحث عن متسابق")},singleLine=true)
        LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)) { items(sorted,key={it.id}){ PersonRow(it,limited,maxScore,{scorePerson=it},{edit=it;editor=true},{people.removeAll{p->p.id==it.id}}) } }
       }
       "الأسئلة العشوائية" -> {
        Text("الأسئلة العشوائية",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
        if(questions.isEmpty()||finished) {
         var expanded by remember{mutableStateOf(false)}
         ExposedDropdownMenuBox(expanded,{expanded=it}) {
          OutlinedTextField(field,{ },Modifier.menuAnchor().fillMaxWidth(),readOnly=true,label={Text("مجال الأسئلة")},trailingIcon={ExposedDropdownMenuDefaults.TrailingIcon(expanded)})
          ExposedDropdownMenu(expanded,{expanded=false}) { (listOf("الكل")+bank.map{it.field}.distinct()).forEach{f->DropdownMenuItem(text={Text(f)},onClick={field=f;expanded=false})} }
         }
         Text("عدد الأسئلة: "+count)
         Slider(count.toFloat(),{count=it.toInt().coerceIn(1,10)},valueRange=1f..10f,steps=8)
         Button(onClick={val pool=if(field=="الكل")bank else bank.filter{it.field==field};questions=pool.shuffled().take(count.coerceAtMost(pool.size));qIndex=0;reveal=false;finished=false;correct=0;wrong=0},Modifier.fillMaxWidth(),enabled=people.isNotEmpty()){Text("ابدأ جولة عشوائية")}
         if(people.isEmpty()) Text("أضف متسابقاً أولاً لبدء الجولة.",color=MaterialTheme.colorScheme.error)
         if(finished) Text("انتهت الجولة — صحيحة: $correct | خاطئة: $wrong",fontWeight=FontWeight.Bold)
        } else {
         var expanded by remember{mutableStateOf(false)}
         ExposedDropdownMenuBox(expanded,{expanded=it}) {
          OutlinedTextField(people.firstOrNull{it.id==selected}?.name ?: "اختر المتسابق",{},Modifier.menuAnchor().fillMaxWidth(),readOnly=true,label={Text("المتسابق")})
          ExposedDropdownMenu(expanded,{expanded=false}) { people.forEach{p->DropdownMenuItem(text={Text(p.name+" — "+p.score)},onClick={selected=p.id;expanded=false})} }
         }
         Text("السؤال "+(qIndex+1)+" من "+questions.size,color=accent,fontWeight=FontWeight.Bold)
         Card(Modifier.fillMaxWidth().padding(vertical=12.dp)) {
          Column(Modifier.padding(18.dp)) {
           Text(questions[qIndex].field,color=accent)
           Text(questions[qIndex].text,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
           if(reveal) Text("الإجابة: "+questions[qIndex].answer,color=accent,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=12.dp))
          }
         }
         if(!reveal) OutlinedButton(onClick={reveal=true},Modifier.fillMaxWidth()){Text("إظهار الإجابة")}
         else {
          Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
           Button(onClick={
            val i=people.indexOfFirst{it.id==selected};if(i>=0){val p=people[i];val s=if(limited)(p.score+1).coerceAtMost(maxScore) else p.score+1;people[i]=p.copy(score=s)}
            correct++;reveal=false;if(qIndex+1>=questions.size)finished=true else qIndex++
           },Modifier.weight(1f),enabled=selected!=null){Text("صحيحة +1")}
           Button(onClick={
            val i=people.indexOfFirst{it.id==selected};if(i>=0){val p=people[i];people[i]=p.copy(score=(p.score-1).coerceAtLeast(0))}
            wrong++;reveal=false;if(qIndex+1>=questions.size)finished=true else qIndex++
           },Modifier.weight(1f),enabled=selected!=null,colors=ButtonDefaults.buttonColors(containerColor=MaterialTheme.colorScheme.error)){Text("خاطئة -1")}
          }
         }
         TextButton(onClick={questions=emptyList();finished=false}){Text("إنهاء الجولة")}
        }
       }
       "الإعدادات" -> {
        Text("الإعدادات",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
        LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp)) {
         item { SettingsCard("لون التطبيق") { colors.forEach{(n,c)->Row(Modifier.fillMaxWidth().clickable{accentName=n}.padding(6.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(22.dp).background(c,CircleShape));Text(n,Modifier.weight(1f));RadioButton(accentName==n,{accentName=n})}} } }
         item { SettingsCard("المظهر") { listOf("system" to "تلقائي","light" to "فاتح","dark" to "داكن").forEach{(v,n)->Row(verticalAlignment=Alignment.CenterVertically){RadioButton(theme==v,{theme=v});Text(n)}} } }
         item { SettingsCard("التصميم") { Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button(onClick={design=1},Modifier.weight(1f)){Text("الأول")};OutlinedButton(onClick={design=2},Modifier.weight(1f)){Text("الثاني")}};Text(if(design==1)"قائمة جانبية وبطاقات." else "لوحة حديثة وشريط تنقل سفلي.") } }
         item { SettingsCard("النقاط") { Row(verticalAlignment=Alignment.CenterVertically){Text("تفعيل الحد الأعلى",Modifier.weight(1f));Switch(limited,{limited=it})};if(limited) OutlinedTextField(maxScore.toString(),{maxScore=it.toIntOrNull()?.coerceAtLeast(1)?:maxScore},label={Text("الحد الأعلى")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number)) } }
        }
       }
      }
     }
    }
   }
   if(editor) EditDialog(edit,{editor=false}){name,score->
    val old=edit
    if(old==null)people.add(Contestant(System.currentTimeMillis(),name,score.coerceAtLeast(0)))
    else {val i=people.indexOfFirst{it.id==old.id};if(i>=0)people[i]=old.copy(name=name,score=score.coerceAtLeast(0))}
    editor=false
   }
   scorePerson?.let{p->ScoreDialog(p,{scorePerson=null}){n,plus->
    val i=people.indexOfFirst{it.id==p.id};if(i>=0){val old=people[i];val v=if(plus)old.score+n else (old.score-n).coerceAtLeast(0);people[i]=old.copy(score=if(limited)v.coerceAtMost(maxScore) else v)}
    scorePerson=null
   }}
  }
 }
}

@Composable private fun Stat(label:String,value:String,modifier:Modifier=Modifier){Card(modifier){Column(Modifier.padding(10.dp)){Text(value,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.primary);Text(label,style=MaterialTheme.typography.labelSmall)}}}
@Composable private fun PersonRow(p:Contestant,limited:Boolean,max:Int,onScore:()->Unit,onEdit:()->Unit,onDelete:()->Unit){
 var menu by remember{mutableStateOf(false)}
 Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp)){Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically){
  Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer,CircleShape),contentAlignment=Alignment.Center){Text(p.name.take(1),fontWeight=FontWeight.Bold)}
  Spacer(Modifier.width(9.dp));Column(Modifier.weight(1f)){Text(p.name,fontWeight=FontWeight.Bold);Text(if(limited)"من أصل $max" else "النقاط",style=MaterialTheme.typography.labelSmall)}
  Text(p.score.toString(),color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.ExtraBold,style=MaterialTheme.typography.titleLarge)
  IconButton(onClick=onScore){Icon(Icons.Default.Tune,null)}
  Box{IconButton(onClick={menu=true}){Icon(Icons.Default.MoreVert,null)};DropdownMenu(menu,{menu=false}){DropdownMenuItem(text={Text("تعديل")},onClick={menu=false;onEdit()});DropdownMenuItem(text={Text("حذف")},onClick={menu=false;onDelete()})}}
 }}
}
@Composable private fun SettingsCard(title:String,content:@Composable ColumnScope.()->Unit){Card(Modifier.fillMaxWidth()){Column(Modifier.padding(14.dp)){Text(title,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleMedium);Spacer(Modifier.height(8.dp));content()}}}
@Composable private fun EditDialog(initial:Contestant?,dismiss:()->Unit,save:(String,Int)->Unit){
 var name by remember(initial){mutableStateOf(initial?.name?:"")};var score by remember(initial){mutableStateOf(initial?.score?.toString()?: "0")}
 AlertDialog(onDismissRequest=dismiss,title={Text(if(initial==null)"إضافة متسابق" else "تعديل متسابق")},text={Column{OutlinedTextField(name,{name=it},label={Text("الاسم")});OutlinedTextField(score,{score=it.filter(Char::isDigit)},label={Text("النقاط")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))}},confirmButton={Button(onClick={if(name.isNotBlank())save(name.trim(),score.toIntOrNull()?:0)},enabled=name.isNotBlank()){Text("حفظ")}},dismissButton={TextButton(onClick=dismiss){Text("إلغاء")}})
}
@Composable private fun ScoreDialog(p:Contestant,dismiss:()->Unit,apply:(Int,Boolean)->Unit){
 var amount by remember(p.id){mutableStateOf("")}
 AlertDialog(onDismissRequest=dismiss,title={Text("نقاط "+p.name)},text={OutlinedTextField(amount,{amount=it.filter(Char::isDigit)},label={Text("العدد")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))},confirmButton={Row{Button(onClick={apply(amount.toInt(),true)},enabled=(amount.toIntOrNull()?:0)>0){Text("زيادة")};Button(onClick={apply(amount.toInt(),false)},enabled=(amount.toIntOrNull()?:0)>0,colors=ButtonDefaults.buttonColors(containerColor=MaterialTheme.colorScheme.error)){Text("خصم")}}},dismissButton={TextButton(onClick=dismiss){Text("إلغاء")}})
}
