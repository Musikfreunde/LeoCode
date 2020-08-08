import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HeaderComponent } from './header/header.component';
import { FileNotFoundComponent } from './file-not-found/file-not-found.component';
import {RouterModule, Routes} from '@angular/router';
import { HomeComponent } from './home/home.component';
import { ExampleComponent } from './example/example.component';
import { ExampleListComponent } from './example-list/example-list.component';
import { SubmissionComponent } from './submission/submission.component';
import { FeedbackComponent } from './feedback/feedback.component';

const appRoutes: Routes = [
  {path: 'home', component: HomeComponent},
  {path: 'examples', component: ExampleListComponent},
  {path: 'example/:id', component: ExampleComponent},
  {path: '**', component: FileNotFoundComponent}
];

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FileNotFoundComponent,
    HomeComponent,
    ExampleComponent,
    ExampleListComponent,
    SubmissionComponent,
    FeedbackComponent
  ],
  imports: [
    BrowserModule,
    RouterModule.forRoot(appRoutes)
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
