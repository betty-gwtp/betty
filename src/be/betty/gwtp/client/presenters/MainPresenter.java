package be.betty.gwtp.client.presenters;

import java.util.HashMap;
import java.util.Map;

import be.betty.gwtp.client.CardHandler;
import be.betty.gwtp.client.CardInView;
import be.betty.gwtp.client.CardSelectionDropControler;
import be.betty.gwtp.client.ClientUtils;
import be.betty.gwtp.client.Storage_access;
import be.betty.gwtp.client.UiConstants;
import be.betty.gwtp.client.action.DeleteCardAction;
import be.betty.gwtp.client.action.DeleteCardActionResult;
import be.betty.gwtp.client.action.GetActivityStateAction;
import be.betty.gwtp.client.action.GetActivityStateActionResult;
import be.betty.gwtp.client.action.GetCards;
import be.betty.gwtp.client.action.GetCardsResult;
import be.betty.gwtp.client.action.GetInstancesOnly;
import be.betty.gwtp.client.action.GetInstancesOnlyResult;
import be.betty.gwtp.client.action.SaveCardDropAction;
import be.betty.gwtp.client.action.SaveCardDropActionResult;
import be.betty.gwtp.client.event.AddNotifEvent;
import be.betty.gwtp.client.event.AddNotifEvent.AddNotifHandler;
import be.betty.gwtp.client.event.BoardViewChangedEvent;
import be.betty.gwtp.client.event.CardFilterEvent;
import be.betty.gwtp.client.event.DeleteCardEvent;
import be.betty.gwtp.client.event.DeleteCardEvent.DeleteCardHandler;
import be.betty.gwtp.client.event.DropCardEvent;
import be.betty.gwtp.client.event.InstancesModifiedEvent;
import be.betty.gwtp.client.event.SetViewEvent;
import be.betty.gwtp.client.event.ShowPlacedCardEvent;
import be.betty.gwtp.client.event.CardFilterEvent.CardFilterHandler;
import be.betty.gwtp.client.event.DropCardEvent.DropCardHandler;
import be.betty.gwtp.client.event.InstancesModifiedEvent.InstancesModifiedHandler;
import be.betty.gwtp.client.event.SelectionCardsModifiedEvent;
import be.betty.gwtp.client.event.SelectionCardsModifiedEvent.SelectionCardsModifiedHandler;
import be.betty.gwtp.client.event.SetViewEvent.SetViewHandler;
import be.betty.gwtp.client.event.ShowPlacedCardEvent.ShowPlacedCardHandler;
import be.betty.gwtp.client.place.NameTokens;
import be.betty.gwtp.client.views.ourWidgets.CardWidget;
import be.betty.gwtp.client.views.ourWidgets.ModifiedVerticalPanel;
import be.betty.gwtp.shared.dto.ActivityState_dto;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.common.client.IndirectProvider;
import com.gwtplatform.common.client.StandardProvider;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

public class MainPresenter extends
		Presenter<MainPresenter.MyView, MainPresenter.MyProxy> {

	public interface MyView extends View {
		public Label getMainLabel();
		public AbsolutePanel getDndPanel();

		//Label getContent();
		//void setContent(Label content);
		ModifiedVerticalPanel getCards_panel();
		void constructFlex(PickupDragController cardDragController);
		ListBox getComboInstance();
		Label getCurrentInstance();
		ListBox getCombo_viewChoice1();
		ListBox getCombo_viewChoice2();
		SimplePanel getBoardPanel();
		public VerticalPanel GetNotifBarVerticalPanel();
	}

	public static final Object SLOT_Card = new Object();
	public static final Object SLOT_BOARD = new Object();
	public static final Object SLOT_OPTION_SELECION = new Object();
	
	@Inject CardSelectionOptionPresenter cardSelectionOptionPresenter;
	@Inject BoardPresenter boardPresenter;
	
	private IndirectProvider<SingleCardPresenter> cardFactory;
	@Inject DispatchAsync dispatcher;

	public static Map<String,CardWidget> allCards;
	private String sem;


	@ProxyCodeSplit
	@NameToken(NameTokens.main)
	public interface MyProxy extends ProxyPlace<MainPresenter> {
	}
	
	
	private Storage stockStore;
	private EventBus eventBus;
	private CardInView cardInView;
	private String project_num;
	public static PickupDragController cardDragController;
	public static CardSelectionDropControler cardDropPanel;
	

	@Inject
	public MainPresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy, final Provider<SingleCardPresenter> provider) {
		super(eventBus, view, proxy);
		cardFactory = new StandardProvider<SingleCardPresenter>(provider);
		stockStore = Storage.getLocalStorageIfSupported();
		this.eventBus = eventBus;
		allCards = new HashMap<String, CardWidget>();
		cardInView = new CardInView(getView().getCombo_viewChoice1(), getView().getCombo_viewChoice2());
	}

	@Override
	protected void revealInParent() {
		// RevealRootContentEvent.fire(this, this);
		RevealContentEvent.fire(this, HeaderPresenter.SLOT_CONTENT, this);
	}

	
	private InstancesModifiedHandler instancesHandler = new InstancesModifiedHandler() {
		@Override public void onInstancesModified(InstancesModifiedEvent event) {
			final int projectId = Storage_access.getSavedProject();
			dispatcher.execute(new GetInstancesOnly(projectId), new AsyncCallback<GetInstancesOnlyResult>() {
				@Override public void onFailure(Throwable arg0) {
					ClientUtils.actionFailed("getting the instance for the project "+projectId+" has failed.");
				}

				@Override public void onSuccess(GetInstancesOnlyResult result) {
					// first we repeuplate localStorage
					int currInstanceBddId = Storage_access.getCurrentProjectInstanceBDDID();
					Storage_access.setProjectInstances(result.getProjectInstances(), 0);
					Storage_access.setCurrentProjectInstanceBddId_fromBddID(currInstanceBddId);
					
					// then we "reset" the instancesPanel
					 writeInstancePanel();
				}
			});
		}
	};
	
	private SetViewHandler setViewHandler = new SetViewHandler() {
		@Override public void onSetView(SetViewEvent event) {
			int index1, index2;
			try {
				index1 = Integer.parseInt(event.getIndex1());
				index2 = Integer.parseInt(event.getIndex2());
			} catch (NumberFormatException e) {
				return;
			}
			
			int realIndex1;
			
			if (index1 == 1)
				realIndex1 = 0;
			else if (index1 == 2)
				realIndex1 = 2;
			else return;
			
			getView().getCombo_viewChoice1().setItemSelected(realIndex1, true);
			printSecondComboBxView(realIndex1);
			getView().getCombo_viewChoice2().setItemSelected(index2, true);
			getEventBus().fireEvent(new BoardViewChangedEvent(index1, index2));
			
		}	
	};
	
	private ShowPlacedCardHandler showPlacedHandler = new ShowPlacedCardHandler() {
		@Override public void onShowPlacedCard(ShowPlacedCardEvent event) {
			getView().getCards_panel().setShowPlacedCard(event.isChecked());
			cardSelectionOptionPresenter.setShowPlacedCard(event.isChecked());
			cardSelectionOptionPresenter.redrawAllCardsFromSelectionPanel();
		}
	};
	
	private DeleteCardHandler deleteCardhandler = new DeleteCardHandler() {
		
		@Override
		public void onDeleteCard(DeleteCardEvent event) {
			System.out.println("dans le main Id de la carte: "+event.getCardId());
			DeleteCardAction action = new DeleteCardAction(event.getSessionId(),
					Integer.parseInt(Storage_access.getBddIdFromCardId(event.getCardId())));
			dispatcher.execute(action, new AsyncCallback<DeleteCardActionResult>() {

				@Override
				public void onFailure(Throwable caught) {
					// TODO Auto-generated method stub
					System.out.println("failed to send the action delete card");
				}

				@Override
				public void onSuccess(DeleteCardActionResult result) {
					System.out.println("the action to delete card send with success");
					onReset();
					
				}
			});
		}
	};
	
	
	
	private CardFilterHandler filterHandler = new CardFilterHandler() {

		@Override public void onCardFilter(CardFilterEvent event) {

//			// TODO : attention, si on decide de faire 2 combobox, les id venan
//			// de la combox et
//			// du local storage ne seront plus les mêmes... et donc on pourra pas
//			// faire ainsi.
//
//			switch (event.getFilterType()) {
//			case TEACHER:
//				writeCardsFromSelector(Filter_kind.TEACHER,
//						Storage_access.getTeacher(event.getFilterObjId()));
//				break;
//			case GROUP:
//				writeCardsFromSelector(Filter_kind.GROUP,
//						Storage_access.getGroup(event.getFilterObjId()));
//			}
		}
	};
	
	private DropCardHandler dropCardHandler = new DropCardHandler() {
		@Override public void onDropCard(DropCardEvent event) {
			
			System.out.println("$$$$$ Catch event.. day="+event.getDay()+" and period= "+event.getPeriod()+" cardid="+event.getCardID());
			int activity_bddId = Storage_access.getBddIdCard(Storage_access.getCard(event.getCardID()));
			int projectInstance = Storage_access.getCurrentProjectInstanceBDDID();
			System.out.println("Actual project instance= "+projectInstance);
			
			// "first", save to bdd (it's asynchronous, so instantaneous)
			dispatcher.execute(new SaveCardDropAction(event.getDay(), event.getPeriod(), activity_bddId, event.getRoom(),
					projectInstance, Storage_access.getSessId()), new AsyncCallback<SaveCardDropActionResult>() {
						@Override public void onFailure(Throwable arg0) {
							System.out.println("save 'dropped card' failed !!");
							System.out.println("tostring"+arg0);
							System.out.println("get message:"+arg0.getMessage());

						}

						@Override public void onSuccess(SaveCardDropActionResult result) {
							System.out.println("save card drop action sucess!!!");

						}
					});

			// Then save in local Storage
			if (event.getDay() != 0) {
				//System.out.println("tiiiittllee"+allCards.get(event.getCardID()).getWidget().getTitle());
				//System.out.println(allCards.size());
			//	allCards.get(event.getCardID()).addStyleName("cardPlaced");
				Storage_access.placeCard(event.getCardID(), event.getDay(), event.getPeriod(),0);
				//TODO: faut aussi l'envoyer � la bdd, ou un truc du genre
			}
			else {
			//	allCards.get(event.getCardID()).addStyleName("card");
				Storage_access.revoveFromSlot(event.getCardID());
				// faut aussi l'envoyer a la bdd, ou un truc du genre
			}
		}
	};
	
	private SelectionCardsModifiedHandler selectCardHandler = new SelectionCardsModifiedHandler () {
		@Override public void onSelectionCardsModified(SelectionCardsModifiedEvent event) {
			for (CardWidget c : allCards.values())
				c.setRightCss();
		}
	};
	
	private AddNotifHandler addNotifHandler = new AddNotifHandler() {
		@Override public void onAddNotif(AddNotifEvent event) {
			Label notification = new Label();
			notification.setText(event.getNotif());
			if (event.getError()==UiConstants.getNotifCss())
				notification.setStyleName("notif");
			else
				notification.setStyleName("notif_error");
			notification.setWidth("150px");
			notification.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			getView().GetNotifBarVerticalPanel().insert(notification,0);
		}
	};
	

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
		project_num = request.getParameter("p", "-1");
		sem = request.getParameter("s", "-1") ;
		// System.out.println("prepare from request: "+name);
		System.out.println("load semestre num "+request.getParameter("s", "-1"));
	}

	@Override
	protected void onBind() {
		super.onBind();
		
		getView().getBoardPanel().setPixelSize((UiConstants.getDayNumber()*UiConstants.getCardWidth())-20, (UiConstants.getPeriodeNumber()*UiConstants.getCardHeight())-7);
		
		getView().getComboInstance().addChangeHandler(new ChangeHandler() {
			@Override public void onChange(ChangeEvent arg0) {
				int selectedIndex = getView().getComboInstance().getSelectedIndex();
				
				getView().getCurrentInstance().setText(
						""+Storage_access.getInstanceLocalNum(selectedIndex)
						);
				
				Storage_access.setCurrentProjectInstanceBddId(selectedIndex);
				
				reDrowStatusCard();
			}});
		
		getView().getCombo_viewChoice1().addChangeHandler(new ChangeHandler() {
			@Override public void onChange(ChangeEvent arg0) {
				printSecondComboBxView(getView().getCombo_viewChoice1().getSelectedIndex());
				
			}

		});
		
		getView().getCombo_viewChoice2().addChangeHandler(new ChangeHandler() {
			@Override public void onChange(ChangeEvent arg0) {
				reDrowStatusCard();
				String txtCbBox1= getView().getCombo_viewChoice1().getItemText(getView().getCombo_viewChoice1().getSelectedIndex());
				String txtCbBox2 = getView().getCombo_viewChoice2().getItemText(getView().getCombo_viewChoice2().getSelectedIndex());
				String notif = "The view of "+txtCbBox1+" "+txtCbBox2+" is selected";
				ClientUtils.notifyUser(notif, UiConstants.getNotifCss(), getEventBus());
			}

		});
		
		set_dnd();
		
		// let's add all the handler (from the evenBus)
		registerHandler(getEventBus().addHandler(CardFilterEvent.getType(),
				filterHandler));
		
		registerHandler(getEventBus().addHandler(
				DropCardEvent.getType(), dropCardHandler));
		
		registerHandler(getEventBus().addHandler(
				InstancesModifiedEvent.getType(), instancesHandler));
		
		registerHandler(getEventBus().addHandler(
				AddNotifEvent.getType(), addNotifHandler));
		
		registerHandler(getEventBus().addHandler(
				SelectionCardsModifiedEvent.getType(), selectCardHandler));
		
		registerHandler(getEventBus().addHandler(
				SetViewEvent.getType(), setViewHandler));
		
		registerHandler(getEventBus().addHandler(
				ShowPlacedCardEvent.getType(), showPlacedHandler));
		
		registerHandler(getEventBus().addHandler(
				DeleteCardEvent.getType(), deleteCardhandler));
		
	}

	private void set_dnd() {

		// create a DragController to manage drag-n-drop actions
		// note: This creates an implicit DropController for the boundary panel
		cardDragController = new PickupDragController(RootPanel.get(), false);
		cardDragController.addDragHandler(new CardHandler(getEventBus()));
		cardDropPanel = new CardSelectionDropControler(getView().getCards_panel(), getEventBus());

		// TODO v�rifier si il y a des lag en utilisant l'application sur le
		// serveur
		// mettre en commentaire ces deux lignes

		// VerticalPanelDropController dropController = new
		// VerticalPanelDropController(getView().getDrop_cards_panel());
		// cardDragController.registerDropController(dropController);

		// dragController.makeDraggable(getView().getDndImage());

		// AbsolutePositionDropController sp = new
		// AbsolutePositionDropController(
		// getView().getDropPanel());
		// IndexedDropController dropController = new
		// IndexedDropController(getView().getDropPanel());

		// dragController.registerDropController(sp);
		// dragController.makeDraggable(getView().getMainLabel());
		// dragController.makeDraggable(getView().getHtml_panel());
		// for (CardPresenter c : allCards)
		// dragController.makeDraggable(c.getView().getWholePanel());

	}

	//@Override
	protected void onReset() {
		// to make a faster reset.. hope it doesn't create bugs..
		/* if (ClientUtils.DONT_REPEAT_YOURSELF)
			ClientUtils.DONT_REPEAT_YOURSELF = false;
		else return; */
		System.out.println("on reset mainPresenter !");
		super.onReset();

		String login = "";
		String sess = "";
		if (stockStore != null) {
			sess = stockStore.getItem("session_id");
			login = stockStore.getItem("login");
		}
		if (sess == null) {
			getView().getMainLabel().setText("Please (re)log first");
			return;
		}

		Storage_access.setProjectName(project_num);
		Storage_access.setSemester(sem);
		getView().getMainLabel().setText("Project Loading ...");//Project: "+Storage_access.getProjectName()+", semestre "+sem);

		//getView().getCombo_viewChoice2().setItemSelected(0, true);
		//ClientUtils.DONT_REPEAT_YOURSELF = {true};  //marche po :(
		GetCards action = new GetCards(project_num, sem, Storage_access.getSessId());
		dispatcher.execute(action, new AsyncCallback<GetCardsResult>() {	
			@Override public void onFailure(Throwable arg0) {
				// TODO Auto-generated method stub
				// arg0.printStackTrace();
				System.err.println("***failure:" + arg0);
				ClientUtils.actionFailed("Operation failed "+arg0);

			}
			@Override public void onSuccess(GetCardsResult result) {
//				if (DONT_REPEAT_YOURSELF[0])
//					DONT_REPEAT_YOURSELF[0]=false;
//				else return;

				Storage_access.populateStorage(project_num,result);
//				//Storage_access.printStorage();
//						
				print_da_page();
//				// getView().getContent().setText(result.getActivities().toString());
				getView().getMainLabel().setText("Project: "+Storage_access.getProjectName()+", semestre "+sem);

			}

		});

	}
	
	@Override protected void onReveal()
	{
		super.onReveal();

		setInSlot(SLOT_OPTION_SELECION, cardSelectionOptionPresenter);
		setInSlot(SLOT_BOARD, boardPresenter);
	}

	/**
	 * PRE: local storage Must (already) be filled with the right info 
	 * POST: The page is drawed on screen
	 * 
	 * Print the cards, the board, with the information in the local storage
	 */
	private void print_da_page() {
		System.out.println("**** Hell yeah, print da page");
		// des Assert ici pour verifier qq truc sur le local storage serait p-e
		// bien..

	//	setInSlot(SLOT_OPTION_SELECION, cardSelectionOptionPresenter);
		cardSelectionOptionPresenter.init();
		
		//setInSlot(SLOT_BOARD, boardPresenter);
		boardPresenter.init(this.cardInView);

		
		cardDragController.registerDropController(cardDropPanel);
		
		
		writeInstancePanel();
		Storage_access.setCurrentProjectInstanceBddId(0);
		
		// for the "view selection" :
		// We write the first combo, then we select it, then we build the second one, we select it and finaly we draw the cards
		setStaticFirstComboView(getView().getCombo_viewChoice1());
		getView().getCombo_viewChoice1().setSelectedIndex(0);
		printSecondComboBxView(getView().getCombo_viewChoice1().getSelectedIndex());
		getView().getCombo_viewChoice2().setSelectedIndex(0);
		
		reDrowStatusCard();
		
		this.boardPresenter.redrawBoard(0,0); //TODO n'enregistrerons nous pas la "vue par default"? ou la derniere ?
		
		writeCardWidgetsFirstTime();
		//getView().constructFlex(cardDragController);
	
		
		
		//CellDropControler dropController = new CellDropControler(simplePanel);
	   //	cardDragController.registerDropController(dropController);

	}

	/**
	 * This method will construct the first combobox involved in selecting a view (for the card board)
	 * It's "static" data, so one should change this with care, as it's used by others (it's handler)
	 */
	private void setStaticFirstComboView(ListBox box) {
		box.clear();
		box.addItem("Professeur");
		box.addItem("Local");
		box.addItem("Classe");
	
	}
	

	private void printSecondComboBxView(int selectedIndex) {
		assert selectedIndex >=0 && selectedIndex<=2;
		getView().getCombo_viewChoice2().clear();
		
		switch (selectedIndex) {
		case 0: 
			for (int i=0; i< Storage_access.getNumberOfTeacher(); i++){
				getView().getCombo_viewChoice2().addItem(Storage_access.getTeacher(i));
			}
			break;
		case 1: 
			for (int i=0; i< Storage_access.getNumberOfRoom(); i++){
				getView().getCombo_viewChoice2().addItem(Storage_access.getRoom(i));
			}
			break;
		case 2: 
			for (int i=0; i< Storage_access.getNumberOfGroup(); i++){
				getView().getCombo_viewChoice2().addItem(Storage_access.getGroup(i)	);
			}
			break;
		default: return;
		}
	}

	private void writeInstancePanel() {
		
		//System.out.println("***mm**  number of instance="+Storage_access.getNumberOfInstance());
		//System.out.println("***mm**  first Instance="+Storage_access.getInstance(0));
		getView().getComboInstance().clear();
		for (int i = 0; i<Storage_access.getNumberOfInstance(); i++)
			getView().getComboInstance().addItem(""+Storage_access.getInstanceLocalNum(i)+": "+Storage_access.getInstanceDesc(i));
		//Storage_access.printStorage();
		
	}

	/**
	 *  PRE: local storage must be full-filled
	 */
	private void writeCardWidgetsFirstTime() {
		setInSlot(SLOT_Card, null);
		allCards.clear();
		for (int i = 0; i < Storage_access.getNumberOfCard(); i++) {
			final int myI = i;
			cardFactory.get(new AsyncCallback<SingleCardPresenter>() {

				@Override
				public void onSuccess (SingleCardPresenter result) {
					result.init(myI);
					addToSlot(SLOT_Card, result);
					CardWidget cardW = result.getView().getCardWidget();
					cardW.setDragControler(cardDragController);
					cardW.makeItDraggable();
					allCards.put(""+myI, cardW);

				}

				@Override
				public void onFailure(Throwable caught) {
					// TODO Auto-generated method stub

				}
			});
		}

	}
//
//	/**
//	 * 
//	 * le principe est d'appliquer "un filtre" au cartons,
//	 * pour en faire "disparaitre" ou "reaparaitre"
//	 * Faut voire si c'est le plus performant/bug free
//	 * ou sinon, on fait comme la methode d'apres..
//	 * 
//	 * @param filter_kind
//	 * @param toFilter
//	 */
//	private void writeCardsFromSelector(Filter_kind filter_kind, String toFilter) {
//
//		for (CardWidget c : allCards) {
//			//System.out.println(c.getView().getHeader());
//			 if(c.getKindString(filter_kind).equals(toFilter))
//				 c.getWidget().setVisible(false);
//		}
//
//	}
//	
	/**
	 *  ( devrait pe bien etre fusionne avec le precedent, ms fonctionne pas de la mm maniere, 
	 *  et surtt, celle ci a besoin d'une requete server.. ms bon, on peu s'arranger)
	 *  
	 *  Le principe est d'appeller cette methode a chaque fois que le
	 *  statu des cartes est modifie (changement de vues, (de mode?), d'instance, (de filtres ?) )
	 *  
	 *  Pour le moment elle n'est (et ne doit etre) utilise lors d'un changemnt d'instances
	 *  
	 */
	private void reDrowStatusCard() {
		int currentInstance= Storage_access.getCurrentProjectInstanceBDDID() ;
		
		dispatcher.execute(new GetActivityStateAction(currentInstance, Storage_access.getSessId()), 
				new AsyncCallback<GetActivityStateActionResult>() {

	

			@Override public void onFailure(Throwable arg0) {
				System.out.println("!!!!!!!!!!!!!!!!!!!!!****  failed to get activities status");

			}

			@Override public void onSuccess(GetActivityStateActionResult result) {
				Storage_access.clearPlacedCard(); // is it the bestWay ?
				for (int i=0; i< Storage_access.getNumberOfCard(); i++) {
					String card = Storage_access.getCard(i);
					ActivityState_dto a = result.getActivitiesState().get(""+Storage_access.getBddIdCard(card));
					if (a == null || a.getDay() == 0 || a.getPeriod() == 0) 
						Storage_access.revoveFromSlot(i);	
					else 
						Storage_access.placeCard(i, a.getDay(), a.getPeriod(),0);	
					
				}
				eventBus.fireEvent( 
						new BoardViewChangedEvent(getView().getCombo_viewChoice1().getSelectedIndex(),
												  getView().getCombo_viewChoice2().getSelectedIndex())
						);
				
				eventBus.fireEvent(new SelectionCardsModifiedEvent());
				//Storage_access.printStorage();
			}

			});

		
	}

	

}
